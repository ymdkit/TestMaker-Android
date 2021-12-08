package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.replaced
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState

@Composable
fun ContentPlaySelectCompleteQuestion(
    state: PlayUiState.SelectComplete,
    onAnswered: (List<String>) -> Unit
) {

    // todo 順序設定
    var yourAnswers: List<OrderedCheckBoxModel> by remember {
        mutableStateOf(List(state.question.answers.count() + state.question.wrongChoices.count()) {
            OrderedCheckBoxModel("", false, 0L, 0)
        })
    }

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
                isSwap = false
            )
            state.choices.forEachIndexed { index, text ->
                TimeStampedCheckbox(
                    text = text,
                    onCheckedChange = { checked, time ->
                        yourAnswers = yourAnswers.replaced(
                            index, OrderedCheckBoxModel(
                                text,
                                checked,
                                time,
                                0,
                            )
                        )
                    })
            }

        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = {
                onAnswered(yourAnswers
                    .filter { it.checked }
                    .sortedBy { it.lastCheckedTime }
                    .map { it.text })
            },
            text = stringResource(R.string.judge_question),
            color = MaterialTheme.colors.secondary
        )
    }
}

@Composable
fun TimeStampedCheckbox(text: String, onCheckedChange: (Boolean, Long) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    var checkedTime by remember { mutableStateOf(System.currentTimeMillis()) }
    Row(modifier = Modifier
        .fillMaxWidth()
        .clickable {
            checkedTime = System.currentTimeMillis()
            checked = !checked
            onCheckedChange(checked, checkedTime)
        }) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Checkbox(
                checked = checked, onCheckedChange = {},
                colors = CheckboxDefaults.colors(
                    MaterialTheme.colors.primary
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
            )
            Text(text = text, modifier = Modifier.padding(start = 8.dp))
        }
    }
}

data class OrderedCheckBoxModel(
    val text: String,
    val checked: Boolean,
    val lastCheckedTime: Long,
    val index: Int,
)