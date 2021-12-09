package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState
import kotlinx.coroutines.delay

@Composable
fun ContentPlayWriteQuestion(
    state: PlayUiState.Write,
    onAnswered: (String) -> Unit,
    isSwap: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(500)
        scrollState.animateScrollTo(1000)
    }

    Column {
        var yourAnswer by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .verticalScroll(
                    scrollState
                )
                .weight(
                    weight = 1f,
                    fill = true
                )
        ) {
            ContentProblem(
                index = state.index,
                question = state.question,
                isSwap = isSwap
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = yourAnswer,
                label = {
                    Text(text = stringResource(R.string.hint_answer_write))
                },
                onValueChange = {
                    yourAnswer = it
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = {
                onAnswered(yourAnswer)
                yourAnswer = ""
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}