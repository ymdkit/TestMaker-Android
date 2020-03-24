package jp.gr.java_conf.foobar.testmaker.service.domain

data class Category(
        val id: Long = -1L,
        val name: String = "",
        val color: Int = 0,
        val order: Int = 0
) {
    companion object {
        fun createFromRealmCategory(from: RealmCategory) =
                Category(
                        id = from.id,
                        name = from.name,
                        color = from.color,
                        order = from.order
                )
    }
}