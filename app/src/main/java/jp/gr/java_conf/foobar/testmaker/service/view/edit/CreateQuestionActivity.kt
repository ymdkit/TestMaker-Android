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
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditCompleteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditSelectCompleteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditSelectQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditWriteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateQuestionActivity : AppCompatActivity() {

    companion object {
        const val ARGUMENT_TEST_ID = "testId"

        fun startActivity(activity: Activity, testId: Long) {
            val intent = Intent(activity, CreateQuestionActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, testId)
            }
            activity.startActivity(intent)
        }
    }

    val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val testViewModel: TestViewModel by viewModel()

    private val workbook: Test by lazy { testViewModel.get(intent.getLongExtra(ARGUMENT_TEST_ID, -1L)) }

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
                                    text = getString(R.string.title_activity_create_question),
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
                                initialPage = 0
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
                                        questionId = -1,
                                        order = -1,
                                        initialProblem = "",
                                        initialAnswer = "",
                                        initialExplanation = "",
                                        initialImageUrl = "",
                                        onCreate = {
                                            testViewModel.create(
                                                test = workbook,
                                                question = it.toQuestion())

                                            showToast(getString(R.string.msg_create_question))
                                        },
                                        buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                        fragmentManager = supportFragmentManager
                                    )
                                    QuestionFormat.SELECT -> ContentEditSelectQuestion(
                                        questionId = -1,
                                        order = -1,
                                        initialProblem = "",
                                        initialAnswer = "",
                                        initialWrongChoices = listOf(),
                                        initialExplanation = "",
                                        initialImageUrl = "",
                                        onCreate = {
                                            testViewModel.create(
                                                test = workbook,
                                                question = it.toQuestion())

                                            showToast(getString(R.string.msg_create_question))
                                        },
                                        buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                        fragmentManager = supportFragmentManager
                                    )
                                    QuestionFormat.COMPLETE ->
                                        ContentEditCompleteQuestion(
                                            questionId = -1,
                                            order = -1,
                                            initialProblem = "",
                                            initialAnswers = listOf(),
                                            initialExplanation = "",
                                            initialImageUrl = "",
                                            onCreate = {
                                                testViewModel.create(
                                                    test = workbook,
                                                    question = it.toQuestion())

                                                showToast(getString(R.string.msg_create_question))
                                            },
                                            buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                            fragmentManager = supportFragmentManager
                                        )
                                    QuestionFormat.SELECT_COMPLETE ->
                                        ContentEditSelectCompleteQuestion(
                                            questionId = -1,
                                            order = -1,
                                            initialProblem = "",
                                            initialAnswers = listOf(),
                                            initialWrongChoices = listOf(),
                                            initialExplanation = "",
                                            initialImageUrl = "",
                                            onCreate = {
                                                testViewModel.create(
                                                    test = workbook,
                                                    question = it.toQuestion())

                                                showToast(getString(R.string.msg_create_question))
                                            },
                                            buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                            fragmentManager = supportFragmentManager
                                        )
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