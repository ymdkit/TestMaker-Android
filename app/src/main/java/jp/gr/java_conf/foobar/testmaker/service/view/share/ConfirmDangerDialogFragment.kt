package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogConfirmDangerBinding

class ConfirmDangerDialogFragment(private val title: String = "", private val buttonText: String = "", private val completion: () -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<DialogConfirmDangerBinding>(inflater, R.layout.dialog_confirm_danger, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = title
            if(buttonText.isNotEmpty()){
                button.text = buttonText
            }
            button.setOnClickListener {
                completion()
                dismiss()
            }
        }.root
    }
}