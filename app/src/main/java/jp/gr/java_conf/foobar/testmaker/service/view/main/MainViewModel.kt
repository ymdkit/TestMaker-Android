package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.*
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class MainViewModel(private val repository: TestMakerRepository, private val auth: Auth, private val preference: SharedPreferenceManager, context: Context) : ViewModel(), PurchasesUpdatedListener, BillingClientStateListener, LifecycleObserver {

    fun getExistingCategoryList() = repository.getExistingCategoriesOfLiveData()
    fun getCategories() = repository.getCategories()
    fun addCategory(category: Category) = repository.addCategory(category)
    fun deleteCategory(category: Category) = repository.deleteCategory(category)
    fun addTest(title: String, colorId: Int, category: String) {
        val test = Test()
        test.title = title
        test.color = colorId
        test.setCategory(category)
        repository.addOrUpdateTest(test)
    }

    fun addOrUpdateTest(test: Test): Long = repository.addOrUpdateTest(test)
    fun getMaxQuestionId(): Long = repository.getMaxQuestionId()
    suspend fun downloadTest(testId: String): FirebaseTestResult = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()
    fun getUser(): FirebaseUser? = auth.getUser()
    fun createUser(user: FirebaseUser?) = repository.setUser(user)
    fun getTests(): LiveData<List<Test>> = repository.getTestsOfLiveData()
    fun fetchTests() = repository.fetchTests()
    fun fetchCategories() = repository.fetchCategories()
    fun sortTests(from: Long, to: Long) = repository.sortTests(from, to)
    fun sortAllTests(mode: Int) = repository.sortAllTests(mode)

    fun migrateSortSetting() {
        repository.sortAllTests(preference.sort)
        preference.sort = -1
    }

    val title: MutableLiveData<String> = MutableLiveData()
    var isEditing: MutableLiveData<Boolean> = MutableLiveData()

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
            if (result.responseCode == BillingClient.BillingResponseCode.OK && skuDetailList.isNotEmpty()) {
                onResponse(skuDetailList[0])
            } else {
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

    fun swapCategories(from: String, to: String) = repository.swapCategories(from, to)
}