package jp.gr.java_conf.foobar.testmaker.service.view.edit.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.example.ui.question.EditQuestionViewModel
import com.example.ui.question.InputQuestionForm
import jp.gr.java_conf.foobar.testmaker.service.R

@Composable
fun EditQuestionForm(
    modifier: Modifier = Modifier,
    viewModel: EditQuestionViewModel,
    fragmentManager: FragmentManager
) {

    val uiState by viewModel.uiState.collectAsState()

    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
    ) {
        InputQuestionForm(
            modifier = Modifier.weight(weight = 1f, fill = true),
            viewModel = viewModel,
            fragmentManager = fragmentManager
        )
        Button(
            enabled = uiState.shouldEnableCreateButton,
            onClick = {
                viewModel.onUpdateButtonClicked()
                focusManager.clearFocus()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(text = stringResource(id = R.string.button_update_question))
        }
    }
}