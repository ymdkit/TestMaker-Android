package jp.gr.java_conf.foobar.testmaker.service.infra.billing

import com.android.billingclient.api.Purchase

sealed class BillingStatus {

    object None : BillingStatus()

    object SetUpSuccess : BillingStatus()

    object BillingFlow : BillingStatus()

    data class PurchaseSuccess(
            val purchases: MutableList<Purchase>? = null
    ) : BillingStatus()

    object ServiceDisconnected : BillingStatus()

    data class Error(val responseCode: Int) : BillingStatus()


}