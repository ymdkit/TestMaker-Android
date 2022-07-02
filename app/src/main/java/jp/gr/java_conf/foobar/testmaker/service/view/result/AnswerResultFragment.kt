package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.QuestionCondition
import com.example.core.utils.Resource
import com.example.ui.answer.ResultViewModel
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.TestMakerTopAppBar
import com.example.ui.core.item.ClickableListItem
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    @Inject
    lateinit var analytics: FirebaseAnalytics

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // 誤って戻らないようにするため、システムの「戻る」を上書きする
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { /* do nothing */ }

        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by viewModel.uiState.collectAsState()
                val studyPlusRecordStatus: ResultViewModel.StudyPlusRecordStatus by viewModel.studyPlusRecordStatus.collectAsState()
                val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
                val scope = rememberCoroutineScope()

                TestMakerAndroidTheme {
                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            ListItem(
                                text = {
                                    Text(
                                        text = stringResource(id = R.string.retry),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                            ClickableListItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "all"
                                    )
                                },
                                text = stringResource(id = R.string.result_dialog_item_retry_all)
                            ) {
                                scope.launch {
                                    drawerState.close()
                                    viewModel.retryQuestions(
                                        questionCondition = QuestionCondition.ALL
                                    )
                                }
                            }
                            ClickableListItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = "wrong"
                                    )
                                },
                                text = stringResource(id = R.string.result_dialog_item_retry_only_incorrect)
                            ) {
                                scope.launch {
                                    drawerState.close()
                                    viewModel.retryQuestions(
                                        questionCondition = QuestionCondition.WRONG
                                    )
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TestMakerTopAppBar(title = stringResource(id = R.string.label_result))
                            },
                            content = {
                                when (val state = uiState) {
                                    is Resource.Success -> {
                                        Surface(color = MaterialTheme.colors.surface) {
                                            Column {
                                                LazyColumn(
                                                    modifier = Modifier
                                                        .padding(16.dp)
                                                        .weight(weight = 1f, fill = true)
                                                ) {
                                                    item {
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
                                                    }
                                                    item {
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
                                                    }
                                                    item {
                                                        Text(
                                                            text = getString(R.string.label_result_questions),
                                                            color = MaterialTheme.colors.primary,
                                                            modifier = Modifier.padding(vertical = 4.dp)
                                                        )
                                                    }

                                                    itemsIndexed(state.value.answeringQuestionList) { index, it ->
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
                                                            analytics.logEvent(
                                                                LogEvent.RESULT_BUTTON_BACK_HOME.eventName
                                                            ) {}
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
                                                            analytics.logEvent(
                                                                LogEvent.RESULT_BUTTON_RETRY.eventName
                                                            ) {}
                                                            scope.launch {
                                                                drawerState.open()
                                                            }
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setup(workbookId = args.workbookId)
        adViewModel.setup()

        requestReview()

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

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.RESULT_SCREEN_OPEN.eventName)
        }
    }
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
    val primaryColor = MaterialTheme.colors.primary

    AndroidView(
        factory = {
            PieChart(it).apply {
                this.data = PieData(dataSet.apply {
                    setDrawValues(false)
                    colors = listOf(primaryColor.toArgb(), Color.GRAY)
                })
                this.centerText = centerText
                setCenterTextSize(24f)
                setCenterTextColor(
                    primaryColor.toArgb()
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