package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState


@Composable
fun ContentPlayManualReviewQuestion(
    state: PlayUiState.ManualReview,
    isSwap: Boolean,
    onJudged: (Boolean) -> Unit,
    onModifyQuestion: (QuestionModel) -> Unit,
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
            ContentReview(
                yourAnswer = "",
                isSwap = isSwap,
                question = state.question,
                onModifyQuestion = onModifyQuestion
            )
        }
        ContainedWideButton(
            modifier = Modifier.padding(bottom = 16.dp),
            onClick = { onJudged(true) },
            text = stringResource(R.string.self_judge_correct),
            color = MaterialTheme.colors.secondary
        )
        ContainedWideButton(
            modifier = Modifier.padding(bottom = 16.dp),
            onClick = { onJudged(false) },
            text = stringResource(R.string.self_judge_incorrect)
        )
    }
}