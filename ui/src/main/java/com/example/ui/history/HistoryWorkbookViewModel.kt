package com.example.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.AnswerHistoryListWatchUseCase
import com.example.usecase.model.AnswerHistoryUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryWorkbookViewModel @Inject constructor(
    private val answerHistoryListWatchUseCase: AnswerHistoryListWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<HistoryWorkbookUiState> = MutableStateFlow(
        HistoryWorkbookUiState(
            answerHistoryList = Resource.Empty
        )
    )
    val uiState: MutableStateFlow<HistoryWorkbookUiState>
        get() = _uiState

    fun setup(workbookId: String) {
        answerHistoryListWatchUseCase.setup(
            workbookId = workbookId,
            scope = viewModelScope
        )

        answerHistoryListWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    answerHistoryList = it
                )
            }
            .launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            answerHistoryListWatchUseCase.load()
        }
}

data class HistoryWorkbookUiState(
    val answerHistoryList: Resource<List<AnswerHistoryUseCaseModel>>
)