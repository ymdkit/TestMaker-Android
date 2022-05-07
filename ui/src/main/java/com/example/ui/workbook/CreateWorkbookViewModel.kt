package com.example.ui.workbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.FolderListWatchUseCase
import com.example.usecase.UserWorkbookCommandUseCase
import com.example.usecase.model.FolderUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private val _uiState: MutableStateFlow<List<FolderUseCaseModel>> =
        MutableStateFlow(emptyList())
    val uiState: StateFlow<List<FolderUseCaseModel>>
        get() = _uiState

    fun setup() {
        folderListWatchUseCase.setup(scope = viewModelScope)

        folderListWatchUseCase.flow
            .onEach {
                _uiState.value = it.getOrNull() ?: emptyList()
            }
            .launchIn(viewModelScope)

    }

    fun load() = viewModelScope.launch {
        folderListWatchUseCase.load()
    }

    fun createWorkbook(name: String, color: Int, folderName: String) =
        viewModelScope.launch {
            userWorkbookCommandUseCase.createWorkbook(name, "", color, folderName)
        }

}
