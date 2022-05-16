package com.example.infra.remote.entity

import android.text.format.DateFormat
import com.example.domain.model.Group
import com.example.domain.model.GroupId
import com.example.domain.model.UserId
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class FirebaseGroup(
    val id: String = "",
    val name: String = "",
    val userId: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {

    companion object {
        fun fromGroup(group: Group) = FirebaseGroup(
            id = group.id.value,
            name = group.name,
            userId = group.userId.value,
            // todo 情報の欠落をどうにかする
            createdAt = Timestamp.now()
        )
    }

    fun getDate(): String {
        val local = Locale.getDefault()
        val format = DateFormat.getBestDateTimePattern(local, "MMMd")
        val dateFormat = SimpleDateFormat(format, local)
        return dateFormat.format(createdAt.seconds * 1000)
    }

    fun toGroup(): Group =
        Group(
            id = GroupId(value = id),
            name = name,
            userId = UserId(value = userId),
            createdAt = getDate()
        )
}