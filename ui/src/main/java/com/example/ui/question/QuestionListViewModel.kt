package com.example.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.UserQuestionCommandUseCase
import com.example.usecase.UserWorkbookCommandUseCase
import com.example.usecase.WorkbookListWatchUseCase
import com.example.usecase.WorkbookWatchUseCase
import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
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
                questionList = Resource.Empty,
                isSearching = false,
                query = "",
                showingMenu = false,
                isExporting = false,
                isSelectMode = false,
                drawerState = QuestionListDrawerState.None
            )
        )
    val uiState: StateFlow<UiState>
        get() = _uiState

    private val _exportWorkbookEvent: Channel<String> = Channel()
    val exportWorkbookEvent: ReceiveChannel<String>
        get() = _exportWorkbookEvent

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
                    questionList = it.map { it.questionList.map { it to false } }
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
            val query = _uiState.value.query
            workbookWatchUseCase.load(
                questionFilter = {
                    it.problem.contains(query) ||
                            it.answers.any { it.contains(query) } ||
                            it.otherSelections.any { it.contains(query) } ||
                            it.explanation.contains(query)
                }
            )
            workbookListWatchUseCase.load()
        }

    fun onSearchButtonClicked(value: Boolean) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = value,
                query = ""
            )
            load()
        }

    fun onQueryChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                query = value
            )
        }

    fun onMenuToggleButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingMenu = !_uiState.value.showingMenu,
            )
        }

    fun onSelectModeChanged(value: Boolean) =
        viewModelScope.launch {
            val currentQuestionList = _uiState.value.questionList.getOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                isSelectMode = value,
                questionList = Resource.Success(currentQuestionList.map { it.first to false })

            )
        }

    fun onQuestionSelected(value: QuestionUseCaseModel) =
        viewModelScope.launch {
            // todo 可読性が悪いのでテストを書く + 書き方を変える
            val currentQuestionList = _uiState.value.questionList.getOrNull() ?: return@launch
            val newQuestionList =
                currentQuestionList.map { if (it.first.id == value.id) it.first to !it.second else it.first to it.second }

            _uiState.value = _uiState.value.copy(
                questionList = Resource.Success(newQuestionList)
            )
        }


    fun onQuestionClicked(value: QuestionUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                drawerState = QuestionListDrawerState.OperateQuestion(value)
            )
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

    fun deleteSelectedQuestionList() =
        viewModelScope.launch {
            val selectedQuestionList =
                _uiState.value.selectedQuestionList?.map { it.id } ?: return@launch
            userQuestionCommandUseCase.deleteQuestions(
                workbookId = workbookId,
                questionIdList = selectedQuestionList
            )
        }

    fun swapQuestions(from: Int, to: Int) =
        viewModelScope.launch {
            val currentQuestionList =
                _uiState.value.questionList.getOrNull()?.map { it.first } ?: return@launch
            val sourceQuestion =
                currentQuestionList[from]
            val destQuestion =
                currentQuestionList[to]

            val newQuestionList = currentQuestionList.map {
                if (it.id == sourceQuestion.id) destQuestion else if (it.id == destQuestion.id) sourceQuestion else it
            }
            _uiState.value = _uiState.value.copy(
                questionList = Resource.Success(newQuestionList.map {
                    it to false
                })
            )

            userQuestionCommandUseCase.swapQuestions(
                sourceQuestionId = sourceQuestion.id,
                destQuestionId = destQuestion.id
            )
        }

    fun onMoveQuestionListButtonClicked() =
        viewModelScope.launch {
            val workbookList = _uiState.value.workbookList.getOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                drawerState = QuestionListDrawerState.SelectMoveDestinationWorkbook(workbookList)
            )
        }

    fun moveQuestionsToOtherWorkbook(
        destWorkbookId: Long,
    ) =
        viewModelScope.launch {
            val selectedQuestionList =
                _uiState.value.selectedQuestionList?.map { it.id } ?: return@launch
            userQuestionCommandUseCase.moveQuestionsToOtherWorkbook(
                sourceWorkbookId = workbookId,
                destWorkbookId = destWorkbookId,
                questionIdList = selectedQuestionList
            )
        }

    fun onCopyQuestionListButtonClicked() =
        viewModelScope.launch {
            val workbookList = _uiState.value.workbookList.getOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                drawerState = QuestionListDrawerState.SelectCopyDestinationWorkbook(workbookList)
            )
        }

    fun copyQuestionsToOtherWorkbook(
        destWorkbookId: Long,
    ) =
        viewModelScope.launch {
            val selectedQuestionList =
                _uiState.value.selectedQuestionList?.map { it.id } ?: return@launch
            userQuestionCommandUseCase.copyQuestionsToOtherWorkbook(
                sourceWorkbookId = workbookId,
                destWorkbookId = destWorkbookId,
                questionIdList = selectedQuestionList
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
        if (_uiState.value.isExporting) return@launch
        _uiState.value = _uiState.value.copy(
            isExporting = true
        )
        val result = userWorkbookCommandUseCase.exportWorkbook(workbookId = workbookId)
        _exportWorkbookEvent.send(result)
        _uiState.value = _uiState.value.copy(
            isExporting = false
        )
    }
}

data class UiState(
    val workbookList: Resource<List<WorkbookUseCaseModel>>,
    val questionList: Resource<List<Pair<QuestionUseCaseModel, Boolean>>>,
    val isSearching: Boolean,
    val query: String,
    val showingMenu: Boolean,
    val isExporting: Boolean,
    val isSelectMode: Boolean,
    val drawerState: QuestionListDrawerState
) {
    val selectedQuestionList = questionList.getOrNull()?.filter { it.second }?.map { it.first }
}

sealed class QuestionListDrawerState {
    object None : QuestionListDrawerState()
    data class OperateQuestion(val question: QuestionUseCaseModel) : QuestionListDrawerState()
    data class SelectMoveDestinationWorkbook(val workbookList: List<WorkbookUseCaseModel>) :
        QuestionListDrawerState()

    data class SelectCopyDestinationWorkbook(val workbookList: List<WorkbookUseCaseModel>) :
        QuestionListDrawerState()
}