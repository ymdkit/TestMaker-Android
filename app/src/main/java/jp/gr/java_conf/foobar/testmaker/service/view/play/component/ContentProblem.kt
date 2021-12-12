package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel

@Composable
fun ContentProblem(
    index: Int,
    question: QuestionModel,
    isSwap: Boolean)
{

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
                text = question.getProblem(isSwap)
            )
        }
        if (question.imageUrl.isNotEmpty()) {
            Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                ContentBitmap(modifier = Modifier.height(200.dp), imageUrl = question.imageUrl)
            }
        }
    }
}
