package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.QuestionCondition
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.DialogMenuItem
import com.example.ui.core.ListDialogFragment
import com.example.usecase.utils.Resource
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class AnswerResultFragment : Fragment() {

    companion object {
        const val COUNT_REQUEST_REVIEW = 5
    }

    private val args: AnswerResultFragmentArgs by navArgs()
    private val testId: Long by lazy { args.workbookId }
    private val duration: Long by lazy { args.duration }

    private val viewModel: ResultViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var logger: TestMakerLogger

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner, object : OnBackPressedCallback(
                true
            ) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack(R.id.page_home, false)
                }
            }
        )

        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by viewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            MyTopAppBar(getString(R.string.label_result))
                        },
                        content = {
                            when (val state = uiState) {
                                is Resource.Success -> {
                                    Surface(color = MaterialTheme.colors.surface) {
                                        Column {
                                            // todo LazyColumn に変更する
                                            Column(
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .verticalScroll(state = ScrollState(0))
                                                    .weight(weight = 1f, fill = true)
                                            ) {
                                                ItemPieChart(
                                                    dataSet =
                                                    PieDataSet(
                                                        listOf(
                                                            state.value.correctCount,
                                                            state.value.incorrectCount
                                                        )
                                                            .map {
                                                                PieEntry(it)
                                                            }, ""
                                                    ),
                                                    centerText = state.value.scoreText
                                                )

                                                val studyPlusRecordStatus: ResultViewModel.StudyPlusRecordStatus by viewModel.studyPlusRecordStatus.collectAsState()

                                                AnimatedVisibility(
                                                    visible = sharedPreferenceManager.uploadStudyPlus == resources.getStringArray(
                                                        R.array.upload_setting_study_plus_values
                                                    )[2] && studyPlusRecordStatus == ResultViewModel.StudyPlusRecordStatus.READY
                                                ) {
                                                    WideOutlinedButton(
                                                        onCLick = {
                                                            viewModel.createStudyPlusRecord(
                                                                duration, requireContext()
                                                            )
                                                        },
                                                        text = getString(R.string.menu_upload_studyplus),
                                                        modifier = Modifier.padding(vertical = 8.dp)
                                                    )
                                                }

                                                Text(
                                                    text = getString(R.string.label_result_questions),
                                                    color = MaterialTheme.colors.primary,
                                                    modifier = Modifier.padding(vertical = 4.dp)
                                                )

                                                state.value.answeringQuestionList.mapIndexed { index, it ->
                                                    ItemResultModel(
                                                        index + 1,
                                                        it.problem,
                                                        it.getSingleLineAnswer(),
                                                        it.answerStatus
                                                    ).ItemResult(onClick = {
                                                        findNavController().navigate(
                                                            AnswerResultFragmentDirections.actionAnswerResultToEditQuestion(
                                                                workbookId = testId,
                                                                questionId = it.id
                                                            )
                                                        )
                                                    })
                                                }
                                            }
                                            Row(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                OutlinedButton(
                                                    modifier = Modifier
                                                        .weight(fill = true, weight = 1f)
                                                        .defaultMinSize(minHeight = 48.dp),
                                                    onClick = {
                                                        findNavController().popBackStack(
                                                            R.id.page_home,
                                                            false
                                                        )
                                                    }) {
                                                    Text(stringResource(R.string.home))
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Button(
                                                    modifier = Modifier
                                                        .weight(fill = true, weight = 1f)
                                                        .defaultMinSize(minHeight = 48.dp),
                                                    colors = ButtonDefaults.buttonColors(
                                                        backgroundColor = MaterialTheme.colors.primary
                                                    ),
                                                    onClick = {
                                                        onClickRetry()
                                                    }
                                                ) {
                                                    Text(stringResource(id = R.string.retry))
                                                }
                                            }
                                            AdView(viewModel = adViewModel)
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setup(workbookId = args.workbookId)
        adViewModel.setup()

        requestReview()

        auth.getUser()?.let { user ->
            viewModel.createAnswerHistory(user)
        }

        when (sharedPreferenceManager.uploadStudyPlus) {
            resources.getStringArray(R.array.upload_setting_study_plus_values)[1] ->
                viewModel.createStudyPlusRecord(duration, requireContext())
        }

        lifecycleScope.launchWhenCreated {
            viewModel.navigateToAnswerWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    findNavController().navigate(
                        AnswerResultFragmentDirections.actionAnswerResultToAnswerWorkbook(
                            workbookId = it.workbookId,
                            isRetry = it.isRetry
                        )
                    )
                }
                .launchIn(this)
        }
    }

    private fun requestReview() {

        sharedPreferenceManager.playCount += 1
        if (sharedPreferenceManager.playCount != COUNT_REQUEST_REVIEW) return

        val manager = ReviewManagerFactory.create(requireContext())
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            if (it.isSuccessful) {
                val reviewInfo = it.result
                manager.launchReviewFlow(requireActivity(), reviewInfo)
            }
        }
    }

    private fun onClickRetry() {
        ListDialogFragment.newInstance(
            getString(R.string.retry),
            listOf(
                DialogMenuItem(
                    title = getString(R.string.result_dialog_item_retry_all),
                    iconRes = R.drawable.ic_play_arrow_white_24dp,
                    action = {
                        viewModel.retryQuestions(
                            questionCondition = QuestionCondition.ALL
                        )
                    }),
                DialogMenuItem(
                    title = getString(R.string.result_dialog_item_retry_only_incorrect),
                    iconRes = R.drawable.ic_baseline_error_24,
                    action = {
                        viewModel.retryQuestions(
                            questionCondition = QuestionCondition.WRONG
                        )
                    })
            )
        ).show(childFragmentManager, "TAG")
    }
}

@Composable
fun MyTopAppBar(title: String) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onPrimary
            )
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
fun WideOutlinedButton(onCLick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onCLick,
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)

    ) {
        Text(text)
    }
}

@Composable
fun ItemPieChart(dataSet: PieDataSet, centerText: String) {
    AndroidView(
        factory = {
            PieChart(it).apply {
                this.data = PieData(dataSet.apply {
                    setDrawValues(false)
                    colors = listOf(R.color.colorAccent, R.color.colorPrimary).map { id ->
                        ContextCompat.getColor(context, id)
                    }
                })
                this.centerText = centerText
                setCenterTextSize(24f)
                setCenterTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                )
                legend.isEnabled = false
                setDrawEntryLabels(false)
                animateXY(500, 500)
                description.isEnabled = false
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    )
}