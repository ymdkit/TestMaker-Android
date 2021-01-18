package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogListMenusBinding

class ListDialogFragment(private val title: String = "", private val menus: List<MenuItem>) : BottomSheetDialogFragment() {

    val controller: ListDialogController by lazy {
        ListDialogController {
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<DialogListMenusBinding>(inflater, R.layout.dialog_list_menus, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = title
            controller.menus = menus
            list.adapter = controller.adapter
        }.root
    }
}