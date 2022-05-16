package com.example.ui.preference

import com.android.billingclient.api.Purchase

sealed class BillingUiState {
    object None : BillingUiState()
    object SetUpSuccess : BillingUiState()
    object BillingFlow : BillingUiState()
    data class PurchaseSuccess(
        val purchases: MutableList<Purchase>? = null
    ) : BillingUiState()

    object ServiceDisconnected : BillingUiState()
    data class Error(val responseCode: Int) : BillingUiState()
}