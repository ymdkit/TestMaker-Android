package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.DialogListMenusBinding

class ListDialogFragment : BottomSheetDialogFragment() {

    companion object {
        private const val KEY_TITLE = "title"

        fun newInstance(title: String, menus: List<DialogMenuItem>) = ListDialogFragment().apply{
            arguments = Bundle().apply {
                putString(KEY_TITLE, title)
            }
            controller.menus = menus
        }
    }

    val controller: ListDialogController by lazy {
        ListDialogController {
            dismiss()
        }
    }

    // Activity の再生成が起きた場合にダイアログを削除する
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null){
            dismiss()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return DataBindingUtil.inflate<DialogListMenusBinding>(inflater, R.layout.dialog_list_menus, container, false).apply {
            lifecycleOwner = viewLifecycleOwner

            this.titleDialog = arguments?.getString(KEY_TITLE)
            list.adapter = controller.adapter
        }.root
    }
}