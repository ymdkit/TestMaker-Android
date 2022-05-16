package com.example.ui.answer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.ContainedWideButton

@Composable
fun ContentPlaySelectQuestion(
    state: PlayUiState.Select,
    onAnswered: (String) -> Unit
) {

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
            items(state.choices) {
                ContainedWideButton(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = {
                        onAnswered(it)
                    },
                    text = it
                )
            }
            item {
                ContainedWideButton(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = {
                        onAnswered("")
                    },
                    text = stringResource(id = R.string.no_answer)
                )
            }
        }
    }
}