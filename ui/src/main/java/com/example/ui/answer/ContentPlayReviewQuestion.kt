package com.example.ui.answer

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.ui.core.ContainedWideButton
import com.example.usecase.model.QuestionUseCaseModel

@Composable
fun ContentPlayReviewQuestion(
    state: PlayUiState.Review,
    isSwap: Boolean,
    onConfirmed: () -> Unit,
    onModifyQuestion: (QuestionUseCaseModel) -> Unit
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
            Column {
                ContentProblem(
                    index = state.index,
                    question = state.question,
                    isSwap = isSwap
                )
                ContentReview(
                    yourAnswer = state.yourAnswer,
                    isSwap = isSwap,
                    question = state.question,
                    onModifyQuestion = onModifyQuestion
                )
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = onConfirmed,
            text = stringResource(R.string.action_next),
            color = MaterialTheme.colors.secondary
        )
    }

}