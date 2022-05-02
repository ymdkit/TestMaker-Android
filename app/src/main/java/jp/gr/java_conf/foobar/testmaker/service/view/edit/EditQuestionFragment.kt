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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.core.AdViewModel
import com.example.ui.core.ComposeAdView
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionModel
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditCompleteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditSelectCompleteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditSelectQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.ContentEditWriteQuestion
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditQuestionFragment : Fragment() {

    private val testViewModel: TestViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    private val args: EditQuestionFragmentArgs by navArgs()

    private val workbook: Test by lazy {
        testViewModel.get(args.workbookId)
    }

    private val question: QuestionModel by lazy {
        workbook.questions.find { it.id == args.questionId }!!
            .toQuestionModel()
    }

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
                                        text = getString(R.string.title_activity_edit_question),
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
                                HorizontalPager(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(weight = 1f, fill = true),
                                    state = pagerState
                                ) { page ->
                                    when (QuestionFormat.values()[page]) {
                                        QuestionFormat.WRITE -> ContentEditWriteQuestion(
                                            questionId = question.id,
                                            order = question.order,
                                            initialProblem = question.problem,
                                            initialAnswer = question.answer,
                                            initialExplanation = question.explanation,
                                            initialImageUrl = question.imageUrl,
                                            onCreate = {
                                                testViewModel.update(it.toQuestion())
                                                requireContext().showToast(getString(R.string.msg_update_question))
                                                findNavController().popBackStack()
                                            },
                                            buttonTitle = stringResource(id = R.string.button_update_question),
                                            fragmentManager = childFragmentManager
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
                                                initialIsAutoGenerateWrongChoices = question.isAutoGenerateWrongChoices,
                                                onCreate = {
                                                    testViewModel.update(it.toQuestion())
                                                    requireContext().showToast(getString(R.string.msg_update_question))
                                                    findNavController().popBackStack()
                                                },
                                                buttonTitle = stringResource(id = R.string.button_update_question),
                                                fragmentManager = childFragmentManager
                                            )
                                        QuestionFormat.COMPLETE ->
                                            ContentEditCompleteQuestion(
                                                questionId = question.id,
                                                order = question.order,
                                                initialProblem = question.problem,
                                                initialAnswers = question.answers,
                                                initialExplanation = question.explanation,
                                                initialIsCheckAnswerOrder = question.isCheckOrder,
                                                initialImageUrl = question.imageUrl,
                                                onCreate = {
                                                    testViewModel.update(it.toQuestion())
                                                    requireContext().showToast(getString(R.string.msg_update_question))
                                                    findNavController().popBackStack()
                                                },
                                                buttonTitle = stringResource(id = R.string.button_update_question),
                                                fragmentManager = childFragmentManager
                                            )
                                        QuestionFormat.SELECT_COMPLETE ->
                                            ContentEditSelectCompleteQuestion(
                                                questionId = question.id,
                                                order = question.order,
                                                initialProblem = question.problem,
                                                initialAnswers = question.answers,
                                                initialWrongChoices = question.wrongChoices,
                                                initialExplanation = question.explanation,
                                                initialImageUrl = question.imageUrl,
                                                initialIsCheckAnswerOrder = question.isCheckOrder,
                                                initialIsAutoGenerateWrongChoices = question.isAutoGenerateWrongChoices,
                                                onCreate = {
                                                    testViewModel.update(it.toQuestion())
                                                    requireContext().showToast(getString(R.string.msg_update_question))
                                                    findNavController().popBackStack()
                                                },
                                                buttonTitle = stringResource(id = R.string.button_update_question),
                                                fragmentManager = childFragmentManager
                                            )
                                    }
                                }
                                ComposeAdView(viewModel = adViewModel)
                            }
                        }
                    )
                }
            }
        }
    }
}