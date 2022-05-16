package com.example.ui.answer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.ui.R
import com.example.ui.core.ContainedWideButton

@Composable
fun ContentPlayManualQuestion(
    state: PlayUiState.Manual,
    isSwap: Boolean,
    onAnswered: () -> Unit
) {
    Column {
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
                question = state.question,
                isSwap = isSwap
            )
        }
        ContainedWideButton(
            onClick = onAnswered,
            text = stringResource(R.string.confirm),
        )
    }
}