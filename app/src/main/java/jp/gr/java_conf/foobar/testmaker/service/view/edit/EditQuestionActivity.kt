package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditCompleteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditSelectQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditWriteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditQuestionActivity : AppCompatActivity() {

    companion object {
        const val ARGUMENT_TEST_ID = "testId"
        const val ARGUMENT_QUESTION_ID = "questionId"

        fun startActivity(activity: Activity, testId: Long, questionId: Long) {
            val intent = Intent(activity, EditQuestionActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, testId)
                putExtra(ARGUMENT_QUESTION_ID, questionId)
            }
            activity.startActivity(intent)
        }
    }

    val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val testViewModel: TestViewModel by viewModel()

    private val workbook: Test by lazy { testViewModel.get(intent.getLongExtra(ARGUMENT_TEST_ID, -1L)) }

    private val question: QuestionModel by lazy {
        workbook.questions.find { it.id == intent.getLongExtra(ARGUMENT_QUESTION_ID, -1L) }!!.toQuestionModel()
    }

    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = getString(R.string.title_activity_edit_question),
                                )
                            },
                            elevation = 0.dp,
                        )
                    },
                    content = {
                        Column {

                            val pagerState = rememberPagerState(
                                pageCount = QuestionFormat.values().size,
                                initialOffscreenLimit = 4,
                                infiniteLoop = false,
                                initialPage = QuestionFormat.values()
                                    .indexOfFirst { it == question.format }
                                    .coerceIn(0, QuestionFormat.values().size)
                            )
                            val tabIndex = pagerState.currentPage
                            val coroutineScope = rememberCoroutineScope()

                            TabRow(selectedTabIndex = tabIndex) {
                                QuestionFormat.values().forEachIndexed { index, format ->
                                    Tab(
                                        selected = tabIndex == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = {
                                            Text(
                                                stringResource(
                                                    id = when (format) {
                                                        QuestionFormat.WRITE -> R.string.write
                                                        QuestionFormat.SELECT -> R.string.select
                                                        QuestionFormat.COMPLETE -> R.string.complete
                                                        QuestionFormat.SELECT_COMPLETE -> R.string.select_complete
                                                    }
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .weight(weight = 1f, fill = true)
                            ) {
                                when (QuestionFormat.values()[pagerState.currentPage]) {
                                    QuestionFormat.WRITE -> ContentEditWriteQuestion(
                                        questionId = question.id,
                                        order = question.order,
                                        initialProblem = question.problem,
                                        initialAnswer = question.answer,
                                        initialExplanation = question.explanation,
                                        initialImageUrl = question.imageUrl,
                                        onCreate = {
                                            testViewModel.update(it.toQuestion())
                                            showToast(getString(R.string.msg_update_question))
                                            finish()
                                        },
                                        buttonTitle = stringResource(id = R.string.button_update_question),
                                        fragmentManager = supportFragmentManager
                                    )
                                    QuestionFormat.SELECT ->
                                        ContentEditSelectQuestion(
                                            questionId = question.id,
                                            order = question.order,
                                            initialProblem = question.problem,
                                            initialAnswer = question.answer,
                                            initialWrongChoices = question.wrongChoices,
                                            initialExplanation = question.explanation,
                                            initialImageUrl = question.imageUrl,
                                            onCreate = {
                                                testViewModel.update(it.toQuestion())
                                                showToast(getString(R.string.msg_update_question))
                                                finish()
                                            },
                                            buttonTitle = stringResource(id = R.string.button_update_question),
                                            fragmentManager = supportFragmentManager
                                        )
                                    QuestionFormat.COMPLETE ->
                                        ContentEditCompleteQuestion(
                                            questionId = question.id,
                                            order = question.order,
                                            initialProblem = question.problem,
                                            initialAnswers = question.answers,
                                            initialExplanation = question.explanation,
                                            initialImageUrl = question.imageUrl,
                                            onCreate = {
                                                testViewModel.update(it.toQuestion())
                                                showToast(getString(R.string.msg_update_question))
                                                finish()
                                            },
                                            buttonTitle = stringResource(id = R.string.button_update_question),
                                            fragmentManager = supportFragmentManager
                                        )
                                    QuestionFormat.SELECT_COMPLETE -> {
                                    }
                                }
                            }
                            ComposeAdView(
                                isRemovedAd = sharedPreferenceManager.isRemovedAd,
                            )
                        }
                    }
                )
            }
        }
    }

}