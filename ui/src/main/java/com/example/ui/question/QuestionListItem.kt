package com.example.ui.question

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.usecase.model.QuestionUseCaseModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun QuestionListItem(
    index: Int,
    question: QuestionUseCaseModel,
    onClick: (QuestionUseCaseModel) -> Unit
) {
    ListItem(
        modifier = Modifier.clickable {
            onClick(question)
        },
        icon = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .padding(0.dp)
            ) {
                Text(text = "${index}")
            }
        },
        text = {
            Text(
                text = question.problem,
                maxLines = 1
            )
        },
        secondaryText = { Text(text = question.getSingleLineAnswer()) }
    )
}