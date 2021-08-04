package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument
import jp.gr.java_conf.foobar.testmaker.service.view.result.ui.theme.TestMakerAndroidTheme

class ComposeConfirmDangerDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_TITLE = "title"
        const val ARG_BUTTON_TEXT = "button_text"
        const val ARG_REQUEST_KEY = "request_key"

        fun newInstance(
            title: String,
            buttonText: String,
            requestKey: String,
        ) = ComposeConfirmDangerDialogFragment().apply {
            arguments = bundleOf(
                ARG_TITLE to title,
                ARG_BUTTON_TEXT to buttonText,
                ARG_REQUEST_KEY to requestKey
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
                    DialogContent(
                        title = requireStringArgument(ARG_TITLE),
                        buttonText = requireStringArgument(ARG_BUTTON_TEXT),
                        onClick = {
                            setFragmentResult(
                                requireStringArgument(ARG_REQUEST_KEY),
                                Bundle.EMPTY
                            )
                            dismiss()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DialogContent(
    title: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = title, color = MaterialTheme.colors.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error
            ),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.align(CenterHorizontally).fillMaxWidth()
        ) {
            Text(
                text = buttonText,
            )
        }
    }
}

@Preview
@Composable
fun PreviewDialog() {
    DialogContent(title = "問題集「ああああ」を削除しますか？", buttonText = "削除する") {}
}