package jp.gr.java_conf.foobar.testmaker.service.extensions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

fun <T> LiveData<T>.observeNonNull(owner: LifecycleOwner, observer: (T) -> Unit) {
    this.observe(owner, {
        if (it != null) {
            observer(it)
        }
    })
}