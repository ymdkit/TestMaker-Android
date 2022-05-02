package com.example.infra.local.entity

import com.example.domain.model.Folder
import com.example.domain.model.FolderId
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

open class RealmCategory : RealmObject() {

    companion object {
        fun fromFolder(folder: Folder): RealmCategory =
            RealmCategory().apply {
                id = folder.id.value
                color = folder.color
                name = folder.name
                order = folder.order
            }
    }

    @PrimaryKey
    var id: Long = 0

    @Required
    var name: String = ""
    var color = 0
    var order = 0

    fun toFolder(workbookCount: Int): Folder = Folder(
        id = FolderId(id),
        name = name,
        color = color,
        order = order,
        workbookCount = workbookCount
    )
}