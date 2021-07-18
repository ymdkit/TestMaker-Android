package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class MainViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val preference: SharedPreferenceManager, context: Context) : ViewModel(), PurchasesUpdatedListener, BillingClientStateListener, LifecycleObserver {

    suspend fun downloadTest(testId: String): FirebaseTest = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test, source = CreateTestSource.DYNAMIC_LINKS.title)

    fun getUser(): FirebaseUser? = auth.getUser()
    fun createUser(user: FirebaseUser?) = repository.setUser(user)

    private val billingClient = BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build()
    private val _billingStatus = MutableLiveData<BillingStatus>()
    val billingStatus: LiveData<BillingStatus> = _billingStatus

    fun purchaseRemoveAd(activity: Activity, billingItem: BillingItem) {
        getSkuDetails(billingItem) {
            val flowPurchase = BillingFlowParams.newBuilder().setSkuDetails(it).build()
            billingClient.launchBillingFlow(activity, flowPurchase)
            _billingStatus.value = BillingStatus.BillingFlow
        }
    }

    private fun getSkuDetails(billingItem: BillingItem, onResponse: (skuDetails: SkuDetails) -> Unit) {

        val skuDetailsParams = SkuDetailsParams.newBuilder()
                .setSkusList(billingItem.skuList())
                .setType(billingItem.skyType)
                .build()

        billingClient.querySkuDetailsAsync(skuDetailsParams) { result, skuDetailList ->

            skuDetailList?.let {
                if (result.responseCode == BillingClient.BillingResponseCode.OK && it.isNotEmpty()) {
                    onResponse(it[0])
                } else {
                    _billingStatus.value = BillingStatus.Error(result.responseCode)
                }
            } ?: run {
                _billingStatus.value = BillingStatus.Error(result.responseCode)
            }
        }

    }

    fun startConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _billingStatus.value = BillingStatus.SetUpSuccess
        } else {
            _billingStatus.value = BillingStatus.Error(billingResult.responseCode)
        }
    }

    override fun onBillingServiceDisconnected() {
        _billingStatus.postValue(BillingStatus.ServiceDisconnected)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()

                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) {}
                    }
                }
            }
            _billingStatus.value = BillingStatus.PurchaseSuccess(purchases)
        } else {
            _billingStatus.value = BillingStatus.Error(billingResult.responseCode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }

    fun removeAd() {
        preference.isRemovedAd = true
    }
}