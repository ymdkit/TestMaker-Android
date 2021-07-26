package jp.gr.java_conf.foobar.testmaker.service.extensions

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.view.share.LoadingDialogFragment
import kotlinx.coroutines.CoroutineScope

fun <T> FragmentActivity.executeJobWithDialog(
        title: String,
        task: suspend CoroutineScope.() -> T,
        onSuccess: (T) -> Unit,
        onFailure: (Throwable) -> Unit
) {

    var dialog: LoadingDialogFragment? = null
    val job = lifecycleScope.launchWhenStarted {
        runCatching {
            task()
        }.onSuccess {
            onSuccess(it)
        }.onFailure {
            onFailure(it)
        }
        dialog?.dismiss()
    }

    val requestKey = "request_job_cancel"
    dialog = LoadingDialogFragment.newInstance(
            title,
            requestKey
    ).also {
        supportFragmentManager.setFragmentResultListener(requestKey, this, { key, _ ->
            if (key != requestKey) return@setFragmentResultListener
            job.cancel()
        })
        it.show(supportFragmentManager, "TAG")
    }
}