package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.FolderListWatchUseCase
import com.example.usecase.UserCommandUseCase
import com.example.usecase.WorkbookListWatchUseCase
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workbookListWatchUseCase: WorkbookListWatchUseCase,
    private val folderListWatchUseCase: FolderListWatchUseCase,
    private val userCommandUseCase: UserCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<Resource<UiState>> =
        MutableStateFlow(Resource.Empty)
    val uiState: StateFlow<Resource<UiState>>
        get() = _uiState

    fun setup() {
        workbookListWatchUseCase.setup(scope = viewModelScope)
        folderListWatchUseCase.setup(scope = viewModelScope)

        combine(
            workbookListWatchUseCase.flow,
            folderListWatchUseCase.flow
        ) { workbookListResource, folderListResource ->
            Resource.merge(workbookListResource, folderListResource) { workbookList, folderList ->

                UiState(
                    workBookList = getNoFolderWorkbookList(workbookList, folderList),
                    folderList = folderList
                )
            }
        }
            .onEach {
                _uiState.value = it
            }.launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            workbookListWatchUseCase.load()
            folderListWatchUseCase.load()
        }

    fun updateFolder(folder: FolderUseCaseModel, newFolderName: String) =
        viewModelScope.launch {
            userCommandUseCase.updateFolder(folder, newFolderName)
        }

    fun deleteFolder(folder: FolderUseCaseModel) =
        viewModelScope.launch {
            userCommandUseCase.deleteFolder(folder)
        }

    fun swapFolder(sourceFolderId: Long, destFolderId: Long) =
        viewModelScope.launch {
            userCommandUseCase.swapFolder(sourceFolderId, destFolderId)
        }

    fun deleteWorkbook(workbook: WorkbookUseCaseModel) =
        viewModelScope.launch {
            userCommandUseCase.deleteWorkbook(workbook)
        }

    fun swapWorkbook(sourceWorkbookId: Long, destWorkbookId: Long) =
        viewModelScope.launch {
            userCommandUseCase.swapWorkbook(sourceWorkbookId, destWorkbookId)
        }

    private fun getNoFolderWorkbookList(
        workBookList: List<WorkbookUseCaseModel>,
        folderList: List<FolderUseCaseModel>
    ) =
        workBookList.filterNot { workbook ->
            folderList.map { it.name }.contains(workbook.folderName)
        }
}

data class UiState(
    val folderList: List<FolderUseCaseModel>,
    val workBookList: List<WorkbookUseCaseModel>
)