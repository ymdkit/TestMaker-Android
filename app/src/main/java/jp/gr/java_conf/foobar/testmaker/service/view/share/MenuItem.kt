package jp.gr.java_conf.foobar.testmaker.service.view.share


data class MenuItem(
        val title: String,
        val iconRes: Int,
        val action: () -> Unit
)