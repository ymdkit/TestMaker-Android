package com.example.ui.preference

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PurchaseViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel(), PurchasesUpdatedListener, BillingClientStateListener {

    private val billingClient =
        BillingClient.newBuilder(context).enablePendingPurchases().setListener(this).build()

    private val _uiState: MutableStateFlow<BillingUiState> =
        MutableStateFlow(BillingUiState.None)
    val uiState: MutableStateFlow<BillingUiState>
        get() = _uiState

    fun setup() {
        billingClient.startConnection(this)
    }

    fun launchBillingFlow(activity: Activity, billingItem: BillingItem) {
        getSkuDetails(billingItem) {
            val flowPurchase = BillingFlowParams.newBuilder().setSkuDetails(it).build()
            billingClient.launchBillingFlow(activity, flowPurchase)
            _uiState.value = BillingUiState.BillingFlow
        }
    }

    private fun getSkuDetails(
        billingItem: BillingItem,
        onResponse: (skuDetails: SkuDetails) -> Unit
    ) {

        val skuDetailsParams = SkuDetailsParams.newBuilder()
            .setSkusList(billingItem.skuList())
            .setType(billingItem.skyType)
            .build()

        billingClient.querySkuDetailsAsync(skuDetailsParams) { result, skuDetailList ->

            skuDetailList?.let {
                if (result.responseCode == BillingClient.BillingResponseCode.OK && it.isNotEmpty()) {
                    onResponse(it[0])
                } else {
                    _uiState.value = BillingUiState.Error(result.responseCode)
                }
            } ?: run {
                _uiState.value = BillingUiState.Error(result.responseCode)
            }
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _uiState.value = BillingUiState.SetUpSuccess
        } else {
            _uiState.value = BillingUiState.Error(billingResult.responseCode)
        }
    }

    override fun onBillingServiceDisconnected() {
        viewModelScope.launch {
            _uiState.value = BillingUiState.ServiceDisconnected
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
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
            _uiState.value = BillingUiState.PurchaseSuccess(purchases)
        } else {
            _uiState.value = BillingUiState.Error(billingResult.responseCode)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingClient.endConnection()
    }
}