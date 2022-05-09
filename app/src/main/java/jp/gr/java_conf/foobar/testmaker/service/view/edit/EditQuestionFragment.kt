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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.QuestionType
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.showToast
import com.example.ui.question.EditQuestionViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.EditQuestionForm
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditQuestionFragment : Fragment() {

    private val args: EditQuestionFragmentArgs by navArgs()
    private val adViewModel: AdViewModel by viewModels()
    private val editQuestionViewModel: EditQuestionViewModel by viewModels()


    @OptIn(ExperimentalPagerApi::class)
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

                                val uiState by editQuestionViewModel.uiState.collectAsState()

                                val pagerState = rememberPagerState(
                                    pageCount = QuestionType.values().size,
                                    initialOffscreenLimit = 4,
                                    infiniteLoop = false,
                                    initialPage = 0
                                )

                                LaunchedEffect(pagerState) {
                                    snapshotFlow { pagerState.currentPage }.collect {
                                        editQuestionViewModel.onQuestionTypeChanged(
                                            QuestionType.valueOf(
                                                it
                                            )
                                        )
                                    }
                                }

                                val coroutineScope = rememberCoroutineScope()

                                TabRow(selectedTabIndex = uiState.questionType.value) {
                                    QuestionType.values().forEachIndexed { index, format ->
                                        Tab(
                                            selected = uiState.questionType.value == index,
                                            onClick = {
                                                coroutineScope.launch {
                                                    pagerState.animateScrollToPage(index)
                                                }
                                            },
                                            text = {
                                                Text(
                                                    stringResource(
                                                        id = when (format) {
                                                            QuestionType.WRITE -> R.string.write
                                                            QuestionType.SELECT -> R.string.select
                                                            QuestionType.COMPLETE -> R.string.complete
                                                            QuestionType.SELECT_COMPLETE -> R.string.select_complete
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
                                ) {
                                    EditQuestionForm(
                                        viewModel = editQuestionViewModel,
                                        fragmentManager = childFragmentManager
                                    )
                                }
                                AdView(viewModel = adViewModel)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editQuestionViewModel.setup(
            workbookId = args.workbookId,
            questionId = args.questionId
        )

        lifecycleScope.launchWhenCreated {
            editQuestionViewModel.onUpdateQuestion
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_update_question))
                    findNavController().popBackStack()
                }
                .launchIn(this)
        }
    }
}