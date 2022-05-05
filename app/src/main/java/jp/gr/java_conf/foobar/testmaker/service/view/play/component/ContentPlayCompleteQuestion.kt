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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.core.utils.replaced
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState
import kotlinx.coroutines.delay

@Composable
fun ContentPlayCompleteQuestion(
    state: PlayUiState.Complete,
    isSwap: Boolean,
    onAnswered: (List<String>) -> Unit)
{
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(500)
        scrollState.animateScrollTo(1000)
    }

    Column {
        var yourAnswers: List<String> by remember {
            mutableStateOf(List(state.question.getAnswers(isSwap).count()) { "" })
        }

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
            yourAnswers.forEachIndexed { index, _ ->
                OutlinedTextField(
                    modifier =
                    if (index == 0) Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(vertical = 8.dp)
                    else Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    value = yourAnswers[index],
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
                        yourAnswers = yourAnswers.replaced(index, text)
                    })
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = {
                onAnswered(yourAnswers)
                yourAnswers = List(state.question.getAnswers(isSwap).count()) { "" }
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}