package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState

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
            color = MaterialTheme.colors.secondary
        )
    }
}