package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.AnswerStatus
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayUiState

@ExperimentalAnimationApi
@Composable
fun ContentPlayReviewQuestion(state: PlayUiState.Review, onConfirmed: () -> Unit) {
    var isShowEffect by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isShowEffect = false
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
            Box {
                Column {
                    ContentProblem(
                        index = state.index,
                        question = state.question
                    )
                    ContentReview(yourAnswer = state.yourAnswer, question = state.question)
                }

                Column {

                    AnimatedVisibility(
                        visible = isShowEffect,
                        enter = fadeIn(
                            animationSpec = tween(durationMillis = 1000)
                        ),
                        exit = fadeOut(
                            animationSpec = tween(durationMillis = 1000)
                        )
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            when (state.question.answerStatus) {
                                AnswerStatus.CORRECT -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_correct),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .height(150.dp)
                                            .width(150.dp),
                                        colorFilter = ColorFilter.tint(MaterialTheme.colors.secondary)
                                    )
                                    Text(
                                        color = MaterialTheme.colors.secondary,
                                        text = stringResource(id = R.string.judge_correct)
                                    )
                                }
                                AnswerStatus.INCORRECT -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_incorrect),
                                        contentDescription = "",
                                        modifier = Modifier
                                            .height(150.dp)
                                            .width(150.dp),
                                        colorFilter = ColorFilter.tint(MaterialTheme.colors.primary)
                                    )
                                    Text(
                                        color = MaterialTheme.colors.primary,
                                        text = stringResource(id = R.string.judge_incorrect)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        ContainedWideButton(
            modifier = Modifier.padding(vertical = 4.dp),
            onClick = onConfirmed,
            text = stringResource(R.string.action_next),
            color = MaterialTheme.colors.secondary
        )
    }

}