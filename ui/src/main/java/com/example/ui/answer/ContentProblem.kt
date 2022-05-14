package com.example.ui.answer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.usecase.model.QuestionUseCaseModel

@Composable
fun ContentProblem(
    index: Int,
    question: QuestionUseCaseModel,
    isSwap: Boolean
) {

    Column {
        Row {
            Text(
                text = "No.${index + 1}",
                color = MaterialTheme.colors.primary
            )
            Spacer(modifier = Modifier.weight(fill = true, weight = 1f))
        }
        SelectionContainer {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = if (isSwap) question.getSingleLineAnswer() else question.problem
            )
        }
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            ContentBitmap(
                image = question.problemImageUrl
            )
        }
    }
}
