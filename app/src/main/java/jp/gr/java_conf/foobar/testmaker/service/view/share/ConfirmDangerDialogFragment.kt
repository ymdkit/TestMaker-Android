package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogConfirmDangerBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument

class ConfirmDangerDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_BUTTON_TEXT = "button_text"

        fun newInstance(
            title: String,
            buttonText: String,
            completion: () -> Unit
        ) = ConfirmDangerDialogFragment()
            .apply {
                this.completion = completion
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_BUTTON_TEXT to buttonText
                )
            }
    }

    private var completion = {}

    // Activity の再生成が起きた場合にダイアログを削除する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return DataBindingUtil.inflate<DialogConfirmDangerBinding>(
            inflater,
            R.layout.dialog_confirm_danger,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = requireStringArgument(ARG_TITLE)
            button.text = requireStringArgument(ARG_BUTTON_TEXT)
            button.setOnClickListener {
                completion()
                dismiss()
            }
        }.root
    }
}