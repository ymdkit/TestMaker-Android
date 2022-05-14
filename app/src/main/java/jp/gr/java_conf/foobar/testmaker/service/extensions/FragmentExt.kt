package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.os.Parcelable
import androidx.fragment.app.Fragment

fun Fragment.requireStringArgument(key: String): String =
    requireArguments().getString(key)!!

fun Fragment.requireIntArgument(key: String): Int =
    requireArguments().getInt(key)

fun <T : Parcelable> Fragment.requireParcelableArgument(key: String): T =
    requireArguments().getParcelable(key)!!