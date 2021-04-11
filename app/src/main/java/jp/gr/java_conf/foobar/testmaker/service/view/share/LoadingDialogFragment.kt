package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogLoadingBinding

class LoadingDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_REQUEST_KEY = "request_key"

        fun newInstance(title: String, requestKey: String) = LoadingDialogFragment().apply {
            arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_REQUEST_KEY to requestKey
            )
        }
    }

    private val title: String by lazy {
        arguments?.getString(ARG_TITLE) ?: throw RuntimeException("title does not exist")
    }

    private val requestKey: String by lazy {
        arguments?.getString(ARG_REQUEST_KEY) ?: throw RuntimeException("requestKey does not exist")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return DataBindingUtil.inflate<DialogLoadingBinding>(inflater, R.layout.dialog_loading, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = title
        }.root
    }

    override fun onCancel(dialog: DialogInterface) {
        setFragmentResult(requestKey, bundleOf())
        super.onCancel(dialog)
    }
}