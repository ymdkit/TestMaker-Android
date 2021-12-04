package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogEditTextBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast

class EditTextDialogFragment: DialogFragment() {

    companion object {

        const val ARG_TITLE = "title"
        const val ARG_DEFAULT_TEXT = "default_text"
        const val ARG_HINT = "hint"

        fun newInstance(
            title: String,
            defaultText: String,
            hint: String,
            completion: (String) -> Unit
        ) = EditTextDialogFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_DEFAULT_TEXT to defaultText,
                ARG_HINT to hint
            )
            this.completion = completion
        }
    }

    private var completion: (String) -> Unit = {}

    // Activity の再生成が起きた場合にダイアログを削除する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val binding = DataBindingUtil.inflate<DialogEditTextBinding>(LayoutInflater.from(activity), R.layout.dialog_edit_text, null, false).apply {
                placeholder = requireStringArgument(ARG_HINT)
                editText.setText(requireStringArgument(ARG_DEFAULT_TEXT))
            }

            MaterialAlertDialogBuilder(it)
                .setTitle(requireStringArgument(ARG_TITLE))
                .setView(binding.root)
                .setPositiveButton(R.string.ok
                ) { _, _ ->
                    if (binding.editText.text.isNullOrEmpty()) {
                        requireContext().showToast(getString(R.string.msg_empty_text))
                        return@setPositiveButton
                    }
                    completion(binding.editText.text.toString())
                }
                .setNegativeButton(R.string.cancel, null)
                .create()

        } ?: throw IllegalStateException("Activity cannot be null")
    }
}