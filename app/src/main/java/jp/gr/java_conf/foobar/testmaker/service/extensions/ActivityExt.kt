package jp.gr.java_conf.foobar.testmaker.service.extensions

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.LoadingDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun <T> FragmentActivity.executeJobWithDialog(
        task: suspend CoroutineScope.() -> T,
        onSuccess: (T) -> Unit,
        onFailure: (Throwable) -> Unit
) {

    var dialog: LoadingDialogFragment? = null
    val job = lifecycleScope.launch {
        runCatching {
            task()
        }.onSuccess {
            onSuccess(it)
        }.onFailure {
            onFailure(it)
        }
        dialog?.dismiss()
    }
    dialog = LoadingDialogFragment(
            title = getString(R.string.msg_creating_invite_group_link),
            onCanceled = {
                showToast(getString(R.string.msg_canceled))
                job.cancel()
            }
    ).also {
        it.show(supportFragmentManager, "TAG")
    }
}