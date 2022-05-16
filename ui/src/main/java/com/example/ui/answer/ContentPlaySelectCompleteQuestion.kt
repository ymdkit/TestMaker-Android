package com.example.ui.answer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.utils.replaced
import com.example.ui.R
import com.example.ui.core.ContainedWideButton


@Composable
fun ContentPlaySelectCompleteQuestion(
    state: PlayUiState.SelectComplete,
    onAnswered: (List<String>) -> Unit
) {

    var yourAnswers: List<String> by remember {
        mutableStateOf(emptyList())
    }

    var isSelectedList: List<Boolean> by remember {
        mutableStateOf(List(state.choices.size) {
            false
        })
    }

    Column {
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
                    isSwap = false
                )
            }
            itemsIndexed(state.choices) { index, text ->
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        isSelectedList = isSelectedList.replaced(index, !isSelectedList[index])
                        yourAnswers = if (!isSelectedList[index]) {
                            yourAnswers.filter { it != text }
                        } else {
                            yourAnswers + listOf(text)
                        }
                    }) {
                    Row(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelectedList[index], onCheckedChange = {
                                isSelectedList =
                                    isSelectedList.replaced(index, !isSelectedList[index])
                                yourAnswers = if (!isSelectedList[index]) {
                                    yourAnswers.filter { it != text }
                                } else {
                                    yourAnswers + listOf(text)
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                MaterialTheme.colors.primary
                            ),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                        )
                        val selectedIndex = yourAnswers.indexOfFirst { it == text }

                        Text(
                            text = if (isSelectedList[index] && state.question.isCheckAnswerOrder) "${selectedIndex + 1} $text" else text,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = {
                onAnswered(yourAnswers)
                yourAnswers = emptyList()
                isSelectedList =
                    List(state.choices.size) {
                        false
                    }
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}