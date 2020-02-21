package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.os.Handler
import android.widget.Button

fun Button.debounceClick(execute: () -> Unit) = setOnClickListener {
    isEnabled = false
    execute()
    Handler().postDelayed({ isEnabled = true }, 600)
}