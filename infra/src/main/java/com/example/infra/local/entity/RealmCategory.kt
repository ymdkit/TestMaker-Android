package com.example.infra.local.entity

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
}