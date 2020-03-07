package jp.gr.java_conf.foobar.testmaker.service.domain

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class Category : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    @Required
    var name: String = ""
    var color = 0
    var order = 0

}