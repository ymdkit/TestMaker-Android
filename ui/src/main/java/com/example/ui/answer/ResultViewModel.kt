package com.example.ui.answer

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AnswerStatus
import com.example.core.QuestionCondition
import com.example.core.utils.Resource
import com.example.ui.R
import com.example.ui.workbook.NavigateToAnswerWorkbookArgs
import com.example.usecase.AnswerSettingWatchUseCase
import com.example.usecase.UserPreferenceCommandUseCase
import com.example.usecase.WorkbookWatchUseCase
import com.example.usecase.model.AnswerSettingUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.studyplus.android.sdk.PostCallback
import jp.studyplus.android.sdk.Studyplus
import jp.studyplus.android.sdk.StudyplusError
import jp.studyplus.android.sdk.record.StudyRecord
import jp.studyplus.android.sdk.record.StudyRecordAmountTotal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val workbookWatchUseCase: WorkbookWatchUseCase,
    private val answerSettingWatchUseCase: AnswerSettingWatchUseCase,
    private val preferenceCommandUseCase: UserPreferenceCommandUseCase,
    private val studyPlus: Studyplus
) : ViewModel() {

    enum class StudyPlusRecordStatus {
        READY,
        RECORDED
    }

    private val _uiState = MutableStateFlow<Resource<ResultUiState>>(Resource.Empty)
    val uiState: StateFlow<Resource<ResultUiState>> = _uiState

    private var workbookId by Delegates.notNull<Long>()

    private val _navigateToAnswerWorkbookEvent: Channel<NavigateToAnswerWorkbookArgs> = Channel()
    val navigateToAnswerWorkbookEvent: ReceiveChannel<NavigateToAnswerWorkbookArgs>
        get() = _navigateToAnswerWorkbookEvent

    val studyPlusRecordStatus = MutableStateFlow(StudyPlusRecordStatus.READY)

    fun setup(
        workbookId: Long
    ) {
        this.workbookId = workbookId
        workbookWatchUseCase.setup(
            workbookId = workbookId,
            scope = viewModelScope
        )
        answerSettingWatchUseCase.setup(
            scope = viewModelScope
        )

        viewModelScope.launch {
            combine(
                workbookWatchUseCase.flow,
                answerSettingWatchUseCase.flow
            ) { workbookFlow, answerSettingFlow ->
                Resource.merge(
                    workbookFlow,
                    Resource.Success(answerSettingFlow)
                ) { workbook, answerSetting ->
                    ResultUiState(
                        workbook = workbook,
                        answerSetting = answerSetting
                    )
                }
            }.onEach {
                _uiState.value = it
            }.launchIn(this)
            workbookWatchUseCase.load()
        }
    }

    fun retryQuestions(questionCondition: QuestionCondition) =
        viewModelScope.launch {
            val workbook = _uiState.value.getOrNull()?.workbook ?: return@launch
            val answerSetting = _uiState.value.getOrNull()?.answerSetting ?: return@launch

            preferenceCommandUseCase.putAnswerSetting(
                answerSetting.copy(
                    questionCondition = questionCondition
                )
            )

            _navigateToAnswerWorkbookEvent.send(
                NavigateToAnswerWorkbookArgs(
                    workbookId = workbook.id,
                    isRetry = true
                )
            )
        }

    fun createStudyPlusRecord(duration: Long, context: Context) =
        viewModelScope.launch(Dispatchers.Default) {

            if (!studyPlus.isAuthenticated()) return@launch
            val state = _uiState.value.getOrNull() ?: return@launch

            val record = StudyRecord(
                duration = (duration / 1000).toInt(),
                amount = StudyRecordAmountTotal(state.answeringQuestionList.size),
                comment = "${state.workbook.name} で勉強しました"
            )

            studyPlusRecordStatus.value = StudyPlusRecordStatus.RECORDED

            studyPlus.postRecord(record,
                object : PostCallback {
                    override fun onSuccess() {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_upload_study_plus),
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }

                    override fun onFailure(e: StudyplusError) {
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(
                                context,
                                context.getString(R.string.msg_upload_study_plus),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                })
        }

}

data class ResultUiState(
    val workbook: WorkbookUseCaseModel,
    val answerSetting: AnswerSettingUseCaseModel
) {
    val answeringQuestionList = workbook.questionList.filter { it.isAnswering }
    val correctCount =
        answeringQuestionList.count { it.answerStatus == AnswerStatus.CORRECT }.toFloat()
    val incorrectCount =
        answeringQuestionList.count { it.answerStatus != AnswerStatus.CORRECT }.toFloat()
    val scoreText = "${correctCount.toInt()}/${answeringQuestionList.size}"
}