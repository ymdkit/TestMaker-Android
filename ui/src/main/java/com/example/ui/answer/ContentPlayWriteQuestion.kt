package com.example.ui.answer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.ui.R
import com.example.ui.core.ContainedWideButton
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContentPlayWriteQuestion(
    state: PlayUiState.Write,
    onAnswered: (String) -> Unit,
    isSwap: Boolean
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        delay(500)
    }

    Column {
        var yourAnswer by remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

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
            item {
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
            item {
                Spacer(modifier = Modifier.height(96.dp))
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 8.dp),
            onClick = {
                onAnswered(yourAnswer)
                yourAnswer = ""
                focusManager.clearFocus()
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}