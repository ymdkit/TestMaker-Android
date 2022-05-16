package com.example.ui.core

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.ui.R
import java.net.UnknownHostException

fun Context.showToast(message: String, length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, length).show()
}

fun Context.showErrorToast(e: Throwable, length: Int = Toast.LENGTH_SHORT) {
    when (e) {
        is UnknownHostException -> {
            Toast.makeText(this, getString(R.string.network_error), length).show()
        }
        else -> {
            Toast.makeText(this, getString(R.string.error), length).show()
            Log.e("ERROR", e.javaClass.simpleName)
        }
    }
}