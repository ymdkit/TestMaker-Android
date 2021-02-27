package jp.gr.java_conf.foobar.testmaker.service.domain

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class History(
        val id: String = "",
        val userId: String = "",
        val userName: String = "",
        val createdAt: Timestamp = Timestamp.now(),
        val numCorrect: Int = 0,
        val numSolved: Int = 0
) {
    fun getDate(): String {
        val local = Locale.getDefault()
        val format = DateFormat.getBestDateTimePattern(local, "MMMd")
        val dateFormat = SimpleDateFormat(format, local)
        return dateFormat.format(createdAt.seconds * 1000)
    }
}