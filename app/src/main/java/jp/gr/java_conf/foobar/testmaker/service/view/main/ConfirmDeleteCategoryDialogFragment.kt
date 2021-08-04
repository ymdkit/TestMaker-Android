package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireParcelableArgument
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument
import jp.gr.java_conf.foobar.testmaker.service.view.result.ui.theme.TestMakerAndroidTheme
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.DangerDialogContent

class ConfirmDeleteCategoryDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_BUTTON_TEXT = "button_text"
        const val ARG_CATEGORY = "arg_category"
        const val ARG_REQUEST_KEY = "request_key"

        const val RESULT_CATEGORY = "result_category"

        fun newInstance(
            title: String,
            buttonText: String,
            requestKey: String,
            category: Category
        ) = ConfirmDeleteCategoryDialogFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_BUTTON_TEXT to buttonText,
                ARG_CATEGORY to category,
                ARG_REQUEST_KEY to requestKey,
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    DangerDialogContent(
                        title = requireStringArgument(ARG_TITLE),
                        buttonText = requireStringArgument(ARG_BUTTON_TEXT),
                        onClick = {
                            setFragmentResult(
                                requireStringArgument(ARG_REQUEST_KEY),
                                bundleOf(
                                    RESULT_CATEGORY to requireParcelableArgument(ARG_CATEGORY)
                                )
                            )
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}