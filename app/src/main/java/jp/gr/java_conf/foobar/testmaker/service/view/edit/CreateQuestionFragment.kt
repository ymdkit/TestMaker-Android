package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
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

class CreateQuestionFragment : Fragment() {

    val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val testViewModel: TestViewModel by viewModel()

    private val args: CreateQuestionFragmentArgs by navArgs()
    private val workbook: Test by lazy { testViewModel.get(args.workbookId) }

    private val logger: TestMakerLogger by inject()

    @ExperimentalPagerApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
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
                                navigationIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .clickable {
                                                findNavController().popBackStack()
                                            }
                                    )
                                }
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

                                                logger.logCreateQuestion(it.toQuestion(),"self")
                                                requireContext().showToast(getString(R.string.msg_create_question))
                                            },
                                            buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                            fragmentManager = childFragmentManager
                                        )
                                        QuestionFormat.SELECT -> ContentEditSelectQuestion(
                                            questionId = -1,
                                            order = -1,
                                            initialProblem = "",
                                            initialAnswer = "",
                                            initialWrongChoices = listOf(),
                                            initialExplanation = "",
                                            initialIsAutoGenerateWrongChoices = false,
                                            initialImageUrl = "",
                                            onCreate = {
                                                testViewModel.create(
                                                    test = workbook,
                                                    question = it.toQuestion())

                                                logger.logCreateQuestion(it.toQuestion(),"self")
                                                requireContext().showToast(getString(R.string.msg_create_question))
                                            },
                                            buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                            fragmentManager = childFragmentManager
                                        )
                                        QuestionFormat.COMPLETE ->
                                            ContentEditCompleteQuestion(
                                                questionId = -1,
                                                order = -1,
                                                initialProblem = "",
                                                initialAnswers = listOf(),
                                                initialExplanation = "",
                                                initialIsCheckAnswerOrder = false,
                                                initialImageUrl = "",
                                                onCreate = {
                                                    testViewModel.create(
                                                        test = workbook,
                                                        question = it.toQuestion())

                                                    logger.logCreateQuestion(it.toQuestion(),"self")
                                                    requireContext().showToast(getString(R.string.msg_create_question))
                                                },
                                                buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                                fragmentManager = childFragmentManager
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
                                                initialIsCheckAnswerOrder = false,
                                                initialIsAutoGenerateWrongChoices = false,
                                                onCreate = {
                                                    testViewModel.create(
                                                        test = workbook,
                                                        question = it.toQuestion())

                                                    logger.logCreateQuestion(it.toQuestion(),"self")
                                                    requireContext().showToast(getString(R.string.msg_create_question))
                                                },
                                                buttonTitle = stringResource(id = R.string.button_create_wuestion),
                                                fragmentManager = childFragmentManager
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
}