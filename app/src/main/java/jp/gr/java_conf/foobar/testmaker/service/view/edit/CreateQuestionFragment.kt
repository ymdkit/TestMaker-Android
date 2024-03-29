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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.ui.core.TestMakerTopAppBar
import com.example.ui.core.showToast
import com.example.ui.logger.LogEvent
import com.example.ui.question.CreateQuestionViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import jp.gr.java_conf.foobar.testmaker.service.view.edit.component.CreateQuestionForm
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class CreateQuestionFragment : Fragment() {

    private val args: CreateQuestionFragmentArgs by navArgs()
    private val adViewModel: AdViewModel by viewModels()
    private val createQuestionViewModel: CreateQuestionViewModel by viewModels()

    @Inject
    lateinit var analytics: FirebaseAnalytics

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
                            TestMakerTopAppBar(
                                navigationIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .clickable {
                                                requireActivity().hideKeyboard(windowToken)
                                                findNavController().popBackStack()
                                            }
                                    )
                                },
                                title = stringResource(id = R.string.title_activity_create_question)
                            )
                        },
                        content = { padding ->
                            Column(
                                modifier = Modifier.padding(padding)
                            ) {
                                val uiState by createQuestionViewModel.uiState.collectAsState()
                                TabRow(
                                    selectedTabIndex = uiState.questionType.value,
                                    backgroundColor = Color.Transparent
                                ) {
                                    QuestionType.values().forEachIndexed { index, format ->
                                        Tab(
                                            selected = uiState.questionType.value == index,
                                            selectedContentColor = MaterialTheme.colors.onBackground,
                                            unselectedContentColor = MaterialTheme.colors.onBackground.copy(
                                                alpha = ContentAlpha.medium
                                            ),
                                            onClick = {
                                                createQuestionViewModel.onQuestionTypeChanged(
                                                    QuestionType.valueOf(
                                                        index
                                                    )
                                                )
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
                                CreateQuestionForm(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(weight = 1f, fill = true),
                                    viewModel = createQuestionViewModel,
                                    fragmentManager = childFragmentManager
                                )
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
        createQuestionViewModel.setup(workbookId = args.workbookId)
        adViewModel.setup()

        lifecycleScope.launchWhenCreated {
            createQuestionViewModel.onCreateQuestion
                .receiveAsFlow()
                .onEach {
                    analytics.logEvent(
                        LogEvent.QUESTIONS_BUTTON_CREATE_QUESTION.eventName
                    ) {}
                    requireContext().showToast(getString(R.string.msg_create_question))
                }
                .launchIn(this)
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(
                FirebaseAnalytics.Param.SCREEN_NAME,
                LogEvent.CREATE_QUESTION_SCREEN_OPEN.eventName
            )
        }
    }
}