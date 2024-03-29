package com.example.ui.answer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.example.usecase.model.QuestionUseCaseModel

@Composable
fun ContentReview(
    yourAnswer: String,
    isSwap: Boolean,
    question: QuestionUseCaseModel,
    onModifyQuestion: (QuestionUseCaseModel) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (yourAnswer.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.label_your_answer),
                color = MaterialTheme.colors.primary
            )
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = yourAnswer
            )
        }
        Text(
            text = stringResource(id = R.string.label_answer),
            color = MaterialTheme.colors.primary
        )
        SelectionContainer {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = if (isSwap) question.problem else question.getMultipleLineAnswer()
            )
        }
        if (question.explanation.isNotEmpty()) {
            Text(
                text = stringResource(id = R.string.label_explanation),
                color = MaterialTheme.colors.primary
            )
            SelectionContainer {
                Text(
                    modifier = Modifier.padding(vertical = 16.dp),
                    text = question.explanation
                )
            }
        }
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            ContentBitmap(image = question.explanationImageUrl)
        }
        Row {
            Spacer(modifier = Modifier.weight(fill = true, weight = 1f))
            OutlinedButton(onClick = {
                onModifyQuestion(question)
            }) {
                Text(stringResource(id = R.string.button_modify_question))
            }
        }
    }
}