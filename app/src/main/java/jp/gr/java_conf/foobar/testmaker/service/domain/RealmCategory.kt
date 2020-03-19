package jp.gr.java_conf.foobar.testmaker.service.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class RealmCategory : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    @Required
    var name: String = ""
    var color = 0
    var order = 0

    companion object {
        fun createFromCategory(from: Category) = RealmCategory().apply {
            id = from.id
            name = from.name
            color = from.color
            order = from.order
        }
    }
}