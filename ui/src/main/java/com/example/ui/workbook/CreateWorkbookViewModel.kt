package com.example.ui.workbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.TestMakerColor
import com.example.usecase.FolderListWatchUseCase
import com.example.usecase.UserWorkbookCommandUseCase
import com.example.usecase.model.FolderUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateWorkbookViewModel @Inject constructor(
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val folderListWatchUseCase: FolderListWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<CreateWorkbookUiState> =
        MutableStateFlow(
            CreateWorkbookUiState(
                folderList = listOf(),
                isImportingWorkbook = false,
                folderName = null
            )
        )
    val uiState: StateFlow<CreateWorkbookUiState>
        get() = _uiState

    private val _importWorkbookCompletionEvent: Channel<String> = Channel()
    val importWorkbookCompletionEvent: ReceiveChannel<String>
        get() = _importWorkbookCompletionEvent

    fun setup(folderName: String) {
        folderListWatchUseCase.setup(scope = viewModelScope)

        folderListWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    folderList = it.getOrNull() ?: emptyList(),
                    folderName = it.getOrNull()?.firstOrNull { it.name == folderName }?.name
                )
            }
            .launchIn(viewModelScope)

    }

    fun load() = viewModelScope.launch {
        folderListWatchUseCase.load()
    }

    fun createWorkbook(name: String, color: TestMakerColor, folderName: String) =
        viewModelScope.launch {
            userWorkbookCommandUseCase.createWorkbook(name, "", color, folderName)
        }

    fun onFolderChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                folderName = value
            )
        }

    fun importWorkbook(
        workbookName: String,
        exportedWorkbook: String,
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isImportingWorkbook = true)
        val workbook = userWorkbookCommandUseCase.importWorkbook(
            workbookName = workbookName,
            exportedWorkbook = exportedWorkbook,
        )
        _uiState.value = _uiState.value.copy(isImportingWorkbook = false)
        _importWorkbookCompletionEvent.send(workbook.name)
    }

}

data class CreateWorkbookUiState(
    val folderList: List<FolderUseCaseModel>,
    val isImportingWorkbook: Boolean,
    val folderName: String?
)
