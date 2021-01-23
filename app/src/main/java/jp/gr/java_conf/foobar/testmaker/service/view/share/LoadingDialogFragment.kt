package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogLoadingBinding

class LoadingDialogFragment(private val title: String = "", private val onCanceled: () -> Unit) : BottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<DialogLoadingBinding>(inflater, R.layout.dialog_loading, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = title
        }.root
    }

    override fun onCancel(dialog: DialogInterface) {
        onCanceled()
        super.onCancel(dialog)
    }
}