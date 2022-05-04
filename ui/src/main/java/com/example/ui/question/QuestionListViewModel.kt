package com.example.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.UserQuestionCommandUseCase
import com.example.usecase.UserWorkbookCommandUseCase
import com.example.usecase.WorkbookListWatchUseCase
import com.example.usecase.WorkbookWatchUseCase
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    private val workbookListWatchUseCase: WorkbookListWatchUseCase,
    private val workbookWatchUseCase: WorkbookWatchUseCase,
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val userQuestionCommandUseCase: UserQuestionCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(
            UiState(
                workbookList = Resource.Empty,
                workbook = Resource.Empty,
                exportedWorkbook = Resource.Empty
            )
        )
    val uiState: StateFlow<UiState>
        get() = _uiState

    private var workbookId by Delegates.notNull<Long>()

    fun setup(workbookId: Long) {
        this.workbookId = workbookId

        workbookListWatchUseCase.setup(
            scope = viewModelScope
        )
        workbookWatchUseCase.setup(
            workbookId = workbookId,
            scope = viewModelScope
        )

        workbookWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    workbook = it
                )
            }.launchIn(viewModelScope)

        workbookListWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    workbookList = it
                )
            }.launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            workbookWatchUseCase.load()
            workbookListWatchUseCase.load()
        }

    fun resetWorkbookAchievement() =
        viewModelScope.launch {
            userWorkbookCommandUseCase.resetWorkbookAchievement(workbookId = workbookId)
        }

    fun deleteQuestions(questionList: List<QuestionUseCaseModel>) =
        viewModelScope.launch {
            userQuestionCommandUseCase.deleteQuestions(
                workbookId = workbookId,
                questionIdList = questionList.map { it.id }
            )
        }

    fun swapQuestions(sourceQuestionId: Long, destQuestionId: Long) =
        viewModelScope.launch {
            userQuestionCommandUseCase.swapQuestions(
                sourceQuestionId = sourceQuestionId,
                destQuestionId = destQuestionId
            )
        }

    fun moveQuestionsToOtherWorkbook(
        destWorkbookId: Long,
        questionList: List<QuestionUseCaseModel>
    ) =
        viewModelScope.launch {
            userQuestionCommandUseCase.moveQuestionsToOtherWorkbook(
                sourceWorkbookId = workbookId,
                destWorkbookId = destWorkbookId,
                questionIdList = questionList.map { it.id }
            )
        }

    fun copyQuestionsToOtherWorkbook(
        destWorkbookId: Long,
        questionList: List<QuestionUseCaseModel>
    ) =
        viewModelScope.launch {
            userQuestionCommandUseCase.copyQuestionsToOtherWorkbook(
                sourceWorkbookId = workbookId,
                destWorkbookId = destWorkbookId,
                questionIdList = questionList.map { it.id }
            )
        }

    fun copyQuestionInSameWorkbook(
        question: QuestionUseCaseModel
    ) = viewModelScope.launch {
        userQuestionCommandUseCase.copyQuestionInSameWorkbook(
            workbookId = workbookId,
            questionId = question.id
        )
    }

    // todo UseCase 層に移動させる + 例外キャッチ
    fun exportWorkbook() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            exportedWorkbook = Resource.Loading
        )
        val result = userWorkbookCommandUseCase.exportWorkbook(workbookId = workbookId)
        _uiState.value = _uiState.value.copy(
            exportedWorkbook = Resource.Success(value = result)
        )
    }
}

data class UiState(
    val workbookList: Resource<List<WorkbookUseCaseModel>>,
    val workbook: Resource<WorkbookUseCaseModel>,
    val exportedWorkbook: Resource<String>,
)