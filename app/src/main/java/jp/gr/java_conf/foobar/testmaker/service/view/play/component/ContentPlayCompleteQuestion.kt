package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState

@Composable
fun ContentPlayCompleteQuestion(state: PlayUiState.Complete, onAnswered: (List<String>) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column {
        var yourAnswers: List<String> by remember {
            mutableStateOf(List(state.question.answers.count()) { "" })
        }

        Column(
            modifier = Modifier
                .verticalScroll(
                    ScrollState(0)
                )
                .weight(
                    weight = 1f,
                    fill = true
                )
        ) {
            ContentProblem(
                index = state.index,
                question = state.question
            )
            yourAnswers.forEachIndexed { index, _ ->
                var yourAnswer by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier =
                    if (index == 0) Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(vertical = 8.dp)
                    else Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    value = yourAnswer,
                    label = {
                        Text(text = stringResource(R.string.hint_answer_write))
                    },
                    keyboardOptions =
                    KeyboardOptions(imeAction = if (index < yourAnswers.size - 1) ImeAction.Next else ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            focusManager.moveFocus(FocusDirection.Down)
                        },
                        onDone = {
                            focusManager.clearFocus()
                        }),
                    onValueChange = { text ->
                        yourAnswer = text
                        yourAnswers = List(state.question.answers.count()) {
                            if (it == index) text else yourAnswers[index]
                        }
                    })
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = {
                onAnswered(yourAnswers)
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}