package jp.gr.java_conf.foobar.testmaker.service.view.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.R

data class ItemResultModel(
    val id: Int,
    val problem: String,
    val answer: String,
    val answerStatus: AnswerStatus
) {

    @ExperimentalMaterialApi
    @Composable
    fun ItemResult(onClick: () -> Unit = {}) {
        Surface(color = MaterialTheme.colors.surface, onClick = onClick) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    id.toString(),
                    modifier = Modifier
                        .width(32.dp)
                        .padding(4.dp),
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp

                )
                Column(Modifier.weight(weight = 1f, fill = true)) {
                    Text(
                        problem,
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                    Text(
                        answer,
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = MaterialTheme.colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 14.sp
                    )
                }
                Image(
                    painterResource(
                        id = if (answerStatus == AnswerStatus.CORRECT) R.drawable.ic_correct else R.drawable.ic_incorrect
                    ),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(if (answerStatus == AnswerStatus.CORRECT) MaterialTheme.colors.secondary else Color.Gray)
                )
            }
        }
    }
}
