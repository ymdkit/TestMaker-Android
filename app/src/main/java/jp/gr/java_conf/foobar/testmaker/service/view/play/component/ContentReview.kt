package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel

@Composable
fun ContentReview(
    yourAnswer: String,
    isSwap: Boolean,
    question: QuestionModel
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
                text = question.getAnswerForReview(isSwap = isSwap)
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
    }
}