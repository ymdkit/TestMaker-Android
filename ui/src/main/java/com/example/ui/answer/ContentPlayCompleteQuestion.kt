package com.example.ui.answer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.ui.R
import com.example.ui.core.ContainedWideButton
import kotlinx.coroutines.job

@Composable
fun ContentPlayCompleteQuestion(
    state: PlayUiState.Complete,
    isSwap: Boolean,
    onAnswered: (List<String>) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        coroutineContext.job.invokeOnCompletion {
            focusRequester.requestFocus()
        }
    }

    Column {
        var yourAnswers: List<String> by remember {
            mutableStateOf(List(state.question.getSwappableAnswers(isSwap).count()) { "" })
        }

        LazyColumn(
            modifier = Modifier
                .weight(
                    weight = 1f,
                    fill = true
                )
        ) {
            item {
                ContentProblem(
                    index = state.index,
                    question = state.question,
                    isSwap = isSwap
                )
            }
            itemsIndexed(yourAnswers) { index, _ ->
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
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = {
                onAnswered(yourAnswers)
                yourAnswers = List(state.question.getSwappableAnswers(isSwap).count()) { "" }
                focusManager.clearFocus()
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}