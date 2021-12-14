package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState

@Composable
fun ContentPlaySelectQuestion(
    state: PlayUiState.Select,
    onAnswered: (String) -> Unit) {

    val scrollState = rememberScrollState()

    Column {
        Column(
            modifier = Modifier
                .verticalScroll(
                    scrollState
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

            state.choices.forEach {
                ContainedWideButton(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = {
                        onAnswered(it)
                    },
                    text = it
                )
            }
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