package jp.gr.java_conf.foobar.testmaker.service.domain

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

data class Group(
        val id: String,
        val name: String,
        val userId: String,
        val createdAt: Timestamp
) {
    fun getDate(): String {
        val local = Locale.getDefault()
        val format = DateFormat.getBestDateTimePattern(local, "MMMd")
        val dateFormat = SimpleDateFormat(format, local)
        return dateFormat.format(createdAt.seconds * 1000)
    }
}