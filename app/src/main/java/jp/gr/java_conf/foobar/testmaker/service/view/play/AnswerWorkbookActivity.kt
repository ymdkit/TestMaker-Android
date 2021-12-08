package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.play.component.*
import jp.gr.java_conf.foobar.testmaker.service.view.result.ComposeResultActivity
import jp.gr.java_conf.foobar.testmaker.service.view.result.MyTopAppBar
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AnswerWorkbookActivity : AppCompatActivity() {

    companion object {

        private const val KEY_WORKBOOK_ID = "workbook_id"
        private const val KEY_IS_RETRY = "is_retry"

        fun startActivity(activity: Activity, workbookId: Long, isRetry: Boolean = false) {
            val intent = Intent(activity, AnswerWorkbookActivity::class.java).apply {
                putExtra(KEY_WORKBOOK_ID, workbookId)
                putExtra(KEY_IS_RETRY, isRetry)
            }
            activity.startActivity(intent)
        }
    }

    private val playViewModel: AnswerWorkbookViewModel by viewModel {
        parametersOf(testId, intent.getBooleanExtra(KEY_IS_RETRY, false))
    }

    val sharedPreferenceManager: SharedPreferenceManager by inject()

    private val testId: Long by lazy { intent.getLongExtra(KEY_WORKBOOK_ID, -1) }

    private val startTime = System.currentTimeMillis()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        MyTopAppBar(getString(R.string.title_activity_play))
                    },
                    content = {
                        val uiState = playViewModel.uiState.collectAsState()

                        // todo フォントサイズ変更
                        Column {
                            Column(
                                modifier = Modifier
                                    .weight(weight = 1f, fill = true)
                                    .padding(16.dp)
                            ) {
                                when (val state = uiState.value) {
                                    is PlayUiState.Write -> {
                                        ContentPlayWriteQuestion(
                                            state = state,
                                            onAnswered = { yourAnswer ->
                                                playViewModel.judgeIsCorrect(
                                                    state.index,
                                                    state.question,
                                                    yourAnswer
                                                )
                                            })
                                    }
                                    is PlayUiState.Select -> {
                                        ContentPlaySelectQuestion(
                                            state = state,
                                            onAnswered = { yourAnswer ->
                                                playViewModel.judgeIsCorrect(
                                                    state.index,
                                                    state.question,
                                                    yourAnswer
                                                )
                                            })
                                    }
                                    is PlayUiState.Complete -> {
                                        ContentPlayCompleteQuestion(
                                            state = state,
                                            onAnswered = { yourAnswers ->
                                                playViewModel.judgeIsCorrect(
                                                    state.index,
                                                    state.question,
                                                    yourAnswers
                                                )
                                            })
                                    }
                                    is PlayUiState.SelectComplete -> {
                                        ContentPlaySelectCompleteQuestion(
                                            state = state,
                                            onAnswered = { yourAnswers ->
                                                playViewModel.judgeIsCorrect(
                                                    state.index,
                                                    state.question,
                                                    yourAnswers
                                                )
                                            })
                                    }
                                    is PlayUiState.Manual -> {
                                        ContentPlayManualQuestion(
                                            state = state,
                                            onAnswered = {
                                                playViewModel.confirm(state.index, state.question)
                                            }
                                        )
                                    }
                                    is PlayUiState.ManualReview -> {
                                        ContentPlayManualReviewQuestion(
                                            state = state,
                                            onJudged = { isCorrect ->
                                                playViewModel.selfJudge(state.question, isCorrect)
                                            }
                                        )
                                    }
                                    is PlayUiState.Review -> {
                                        ContentPlayReviewQuestion(
                                            state = state,
                                            onConfirmed = {
                                                playViewModel.loadNext()
                                            }
                                        )
                                    }
                                    is PlayUiState.Finish -> {
                                        ComposeResultActivity.startActivity(
                                            this@AnswerWorkbookActivity,
                                            testId,
                                            System.currentTimeMillis() - startTime
                                        )
                                    }
                                    is PlayUiState.NoQuestionExist -> {
                                        showToast(stringResource(id = R.string.msg_empty_question))
                                        finish()
                                    }
                                }
                            }
                            ComposeAdView(isRemovedAd = sharedPreferenceManager.isRemovedAd)
                        }
                    }
                )

            }
        }
    }

    override fun onBackPressed() {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(R.string.play_dialog_confirm_interrupt),
            buttonText = getString(R.string.ok)
        ) {
            super.onBackPressed()
        }.show(supportFragmentManager, "TAG")
    }
}