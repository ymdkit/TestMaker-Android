package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogEditTextBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast

class EditTextDialogFragment(
        private val title: String = "",
        private val defaultText: String = "",
        private val hint: String,
        private val completion: (String) -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val binding = DataBindingUtil.inflate<DialogEditTextBinding>(LayoutInflater.from(activity), R.layout.dialog_edit_text, null, false).apply {
                placeholder = hint
                editText.setText(defaultText)
            }

            AlertDialog.Builder(it, R.style.MyAlertDialogStyle)
                    .setTitle(title)
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