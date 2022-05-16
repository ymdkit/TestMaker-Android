package com.example.ui.preference


data class BillingItem(
        val sku: String,
        val skyType: String
) {

    fun skuList(): List<String> = listOf(sku)

}
