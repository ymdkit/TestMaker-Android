package jp.gr.java_conf.foobar.testmaker.service.extensions

import androidx.fragment.app.Fragment

fun Fragment.requireStringArgument(key: String): String =
    requireArguments().getString(key) ?: throw NullPointerException("${this.javaClass.name}: $key is null")