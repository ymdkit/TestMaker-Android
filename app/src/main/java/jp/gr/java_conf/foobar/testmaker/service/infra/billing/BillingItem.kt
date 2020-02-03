package jp.gr.java_conf.foobar.testmaker.service.infra.billing


data class BillingItem(
        val sku: String,
        val skyType: String
) {

    fun skuList(): List<String> = listOf(sku)

}
