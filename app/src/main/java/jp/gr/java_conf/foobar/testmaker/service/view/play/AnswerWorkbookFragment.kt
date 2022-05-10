package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.answer.*
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.FadeInAndOutAnimation
import com.example.ui.core.showToast
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.android.gms.ads.AdSize
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.result.MyTopAppBar
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import javax.inject.Inject

@AndroidEntryPoint
class AnswerWorkbookFragment : Fragment() {

    private val args: AnswerWorkbookFragmentArgs by navArgs()
    private val playViewModel: AnswerWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val testId: Long by lazy { args.workbookId }

    private val startTime = System.currentTimeMillis()

    private var soundCorrect: MediaPlayer? = null
    private var soundIncorrect: MediaPlayer? = null

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

        playViewModel.setup(
            testId,
            args.isRetry
        )

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(
                true
            ) {
                override fun handleOnBackPressed() {
                    ConfirmDangerDialogFragment.newInstance(
                        title = getString(R.string.play_dialog_confirm_interrupt),
                        buttonText = getString(R.string.ok)
                    ) {
                        findNavController().popBackStack()
                    }.show(childFragmentManager, "TAG")
                }
            }
        )

        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            MyTopAppBar(getString(R.string.title_activity_play))
                        },
                        content = {
                            val uiState = playViewModel.uiState.collectAsState()
                            val effectState = playViewModel.answerEffectState.collectAsState()

                            // todo フォントサイズ変更
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
                                                    isSwap = sharedPreferenceManager.reverse,
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
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onAnswered = {
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
                                                            workbookId = testId,
                                                            questionId = it.id
                                                        ))
                                                    }
                                                )
                                            }
                                            is PlayUiState.Review -> {
                                                ContentPlayReviewQuestion(
                                                    state = state,
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onConfirmed = {
                                                        playViewModel.loadNext(state.index)
                                                    },
                                                    onModifyQuestion = {
                                                        findNavController().navigate(AnswerWorkbookFragmentDirections.actionAnswerWorkbookToEditQuestion(
                                                            workbookId = testId,
                                                            questionId = it.id
                                                        ))
                                                    }
                                                )
                                            }
                                            is PlayUiState.Finish -> {
                                                findNavController().navigate(AnswerWorkbookFragmentDirections.actionAnswerWorkbookToAnswerResult(
                                                    workbookId = testId,
                                                    duration = System.currentTimeMillis() - startTime
                                                ))
                                            }
                                            is PlayUiState.NoQuestionExist -> {
                                                requireContext().showToast(stringResource(id = R.string.msg_empty_question))
                                                findNavController().popBackStack()
                                            }
                                            is PlayUiState.WaitingNextQuestion -> {
                                                ContentProblem(index = state.index, question = state.question, isSwap = sharedPreferenceManager.reverse)
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
    }

    override fun onDestroy() {
        super.onDestroy()
        soundCorrect?.release()
        soundCorrect = null
        soundIncorrect?.release()
        soundIncorrect = null
    }
}