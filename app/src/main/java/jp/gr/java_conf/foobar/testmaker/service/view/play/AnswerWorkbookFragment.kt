package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.ads.AdSize
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.view.play.component.*
import jp.gr.java_conf.foobar.testmaker.service.view.result.ComposeResultActivity
import jp.gr.java_conf.foobar.testmaker.service.view.result.MyTopAppBar
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AnswerWorkbookFragment : Fragment() {

    private val args: AnswerWorkbookFragmentArgs by navArgs()
    private val playViewModel: AnswerWorkbookViewModel by viewModel {
        parametersOf(testId, args.isRetry)
    }
    val sharedPreferenceManager: SharedPreferenceManager by inject()

    private val testId: Long by lazy { args.workbookId }

    private val startTime = System.currentTimeMillis()

    private var soundCorrect: MediaPlayer? = null
    private var soundIncorrect: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundCorrect = MediaPlayer.create(requireContext(), R.raw.correct)
        soundIncorrect = MediaPlayer.create(requireContext(), R.raw.mistake)
    }

    @ExperimentalAnimationApi
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
                                                    }
                                                )
                                            }
                                            is PlayUiState.Review -> {
                                                ContentPlayReviewQuestion(
                                                    state = state,
                                                    isSwap = sharedPreferenceManager.reverse,
                                                    onConfirmed = {
                                                        playViewModel.loadNext(state.index)
                                                    }
                                                )
                                            }
                                            is PlayUiState.Finish -> {
                                                ComposeResultActivity.startActivity(
                                                    requireActivity(),
                                                    testId,
                                                    System.currentTimeMillis() - startTime
                                                )
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
                                ComposeAdView(
                                    isRemovedAd = sharedPreferenceManager.isRemovedAd,
                                    adSize = AdSize.LARGE_BANNER
                                )
                            }
                        }
                    )

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundCorrect?.release()
        soundCorrect = null
        soundIncorrect?.release()
        soundIncorrect = null
    }
}