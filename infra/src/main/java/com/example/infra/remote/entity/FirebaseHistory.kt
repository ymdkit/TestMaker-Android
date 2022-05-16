package com.example.infra.remote.entity

import android.text.format.DateFormat
import com.example.domain.model.AnswerHistory
import com.example.domain.model.DocumentId
import com.example.domain.model.UserId
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class FirebaseHistory(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val numCorrect: Int = 0,
    val numSolved: Int = 0
) {

    companion object {
        fun fromHistory(history: AnswerHistory) =
            FirebaseHistory(
                id = history.id.value,
                userId = history.userId.value,
                userName = history.userName,
                createdAt = Timestamp.now(),
                numCorrect = history.numCorrect,
                numSolved = history.numSolved
            )
    }

    fun getDate(): String {
        val local = Locale.getDefault()
        val format = DateFormat.getBestDateTimePattern(local, "MMMd")
        val dateFormat = SimpleDateFormat(format, local)
        return dateFormat.format(createdAt.seconds * 1000)
    }

    fun toAnswerHistory() =
        AnswerHistory(
            id = DocumentId(value = id),
            userId = UserId(value = userId),
            userName = userName,
            createdAt = getDate(),
            numCorrect = numCorrect,
            numSolved = numSolved

        )
}