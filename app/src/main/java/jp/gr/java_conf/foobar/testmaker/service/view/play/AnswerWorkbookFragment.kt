package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.answer.*
import com.example.ui.core.*
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.android.gms.ads.AdSize
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import javax.inject.Inject

@AndroidEntryPoint
class AnswerWorkbookFragment : Fragment() {

    private val args: AnswerWorkbookFragmentArgs by navArgs()
    private val playViewModel: AnswerWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val startTime = System.currentTimeMillis()

    private var soundCorrect: MediaPlayer? = null
    private var soundIncorrect: MediaPlayer? = null

    @Inject
    lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundCorrect = MediaPlayer.create(requireContext(), R.raw.correct)
        soundIncorrect = MediaPlayer.create(requireContext(), R.raw.mistake)
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 誤って戻らないようにするため、システムの「戻る」を上書きする
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { /* do nothing */ }

        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.title_activity_play),
                                actions = {
                                    ConfirmActionTextButton(
                                        label = stringResource(id = R.string.finish_answer),
                                        confirmMessage = stringResource(id = R.string.msg_finish_answer),
                                        confirmButtonText = stringResource(id = R.string.button_finish)
                                    ) {
                                        analytics.logEvent(
                                            LogEvent.ANSWER_BUTTON_END.eventName
                                        ) {}
                                        requireActivity().hideKeyboard(windowToken)
                                        findNavController().navigate(
                                            AnswerWorkbookFragmentDirections.actionAnswerWorkbookToAnswerResult(
                                                workbookId = args.workbookId,
                                                duration = System.currentTimeMillis() - startTime
                                            )
                                        )
                                    }
                                }
                            )
                        },
                        content = {
                            val uiState = playViewModel.uiState.collectAsState()
                            val effectState = playViewModel.answerEffectState.collectAsState()

                            Column {
                                Box(modifier = Modifier.weight(weight = 1f, fill = true)) {
                                    Column(
                                        modifier = Modifier
                                            .padding(16.dp)
                                    ) {
                                        when (val state = uiState.value) {
                                            is PlayUiState.Write -> {
                                                ContentPlayWriteQuestion(
                                                    state = state,
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onAnswered = { yourAnswer ->
                                                        analytics.logEvent(
                                                            LogEvent.ANSWER_SHOW_QUESTION.eventName
                                                        ) {}
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
                                                        analytics.logEvent(
                                                            LogEvent.ANSWER_SHOW_QUESTION.eventName
                                                        ) {}
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
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onAnswered = { yourAnswers ->
                                                        analytics.logEvent(
                                                            LogEvent.ANSWER_SHOW_QUESTION.eventName
                                                        ) {}
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
                                                        analytics.logEvent(
                                                            LogEvent.ANSWER_SHOW_QUESTION.eventName
                                                        ) {}
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
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onAnswered = {
                                                        analytics.logEvent(
                                                            LogEvent.ANSWER_SHOW_QUESTION.eventName
                                                        ) {}
                                                        playViewModel.confirm(
                                                            state.index,
                                                            state.question
                                                        )
                                                    }
                                                )
                                            }
                                            is PlayUiState.ManualReview -> {
                                                ContentPlayManualReviewQuestion(
                                                    state = state,
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onJudged = { isCorrect ->
                                                        playViewModel.selfJudge(
                                                            state.index,
                                                            state.question,
                                                            isCorrect
                                                        )
                                                    },
                                                    onModifyQuestion = {
                                                        findNavController().navigate(AnswerWorkbookFragmentDirections.actionAnswerWorkbookToEditQuestion(
                                                            workbookId = args.workbookId,
                                                            questionId = it.id
                                                        ))
                                                    }
                                                )
                                            }
                                            is PlayUiState.Review -> {
                                                ContentPlayReviewQuestion(
                                                    state = state,
                                                    isSwap = sharedPreferenceManager.reverse && state.question.isReversible,
                                                    onConfirmed = {
                                                        playViewModel.loadNext(state.index)
                                                    },
                                                    onModifyQuestion = {
                                                        findNavController().navigate(
                                                            AnswerWorkbookFragmentDirections.actionAnswerWorkbookToEditQuestion(
                                                                workbookId = args.workbookId,
                                                                questionId = it.id
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                            is PlayUiState.Finish -> {
                                                requireActivity().hideKeyboard(windowToken)
                                                findNavController().navigate(
                                                    AnswerWorkbookFragmentDirections.actionAnswerWorkbookToAnswerResult(
                                                        workbookId = args.workbookId,
                                                        duration = System.currentTimeMillis() - startTime
                                                    )
                                                )
                                            }
                                            is PlayUiState.NoQuestionExist -> {
                                                analytics.logEvent(
                                                    LogEvent.ANSWER_ERROR_QUESTIONS.eventName
                                                ) {}
                                                requireContext().showToast(stringResource(id = R.string.msg_empty_question))
                                                findNavController().popBackStack()
                                            }
                                            is PlayUiState.WaitingNextQuestion -> {
                                                ContentProblem(
                                                    index = state.index,
                                                    question = state.question,
                                                    isSwap = sharedPreferenceManager.reverse && state.question.isReversible
                                                )
                                            }
                                        }
                                    }
                                    when (effectState.value) {
                                        AnswerEffectState.Correct -> {
                                            if (sharedPreferenceManager.audio) {
                                                soundCorrect?.start()
                                            }
                                            FadeInAndOutAnimation {
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
                                        }

                                        AnswerEffectState.Incorrect -> {
                                            if (sharedPreferenceManager.audio) {
                                                soundIncorrect?.start()
                                            }
                                            FadeInAndOutAnimation {
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
                                        else -> {
                                        }
                                    }
                                }
                                AdView(
                                    viewModel = adViewModel,
                                    adSize = AdSize.LARGE_BANNER
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adViewModel.setup()

        playViewModel.setup(
            args.workbookId,
            args.isRetry
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        soundCorrect?.release()
        soundCorrect = null
        soundIncorrect?.release()
        soundIncorrect = null
    }


    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.ANSWER_SCREEN_OPEN.eventName)
        }
    }
}