package com.example.ui.workbook

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.home.NavigateToAnswerWorkbookArgs
import com.example.usecase.*
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.SharedWorkbookUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class WorkbookListViewModel @Inject constructor(
    private val workbookListWatchUseCase: WorkbookListWatchUseCase,
    private val folderListWatchUseCase: FolderListWatchUseCase,
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val userFolderCommandUseCase: UserFolderCommandUseCase,
    private val answerSettingGetUseCase: AnswerSettingWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<WorkbookListUiState> =
        MutableStateFlow(
            WorkbookListUiState(
                resources = Resource.Empty,
                workbookListDrawerState = WorkbookListDrawerState.None
            )
        )
    val uiState: StateFlow<WorkbookListUiState>
        get() = _uiState

    private val _navigateToAnswerWorkbookEvent: Channel<NavigateToAnswerWorkbookArgs> = Channel()
    val navigateToAnswerWorkbookEvent: ReceiveChannel<NavigateToAnswerWorkbookArgs>
        get() = _navigateToAnswerWorkbookEvent

    private val _questionListEmptyEvent: Channel<Unit> = Channel()
    val questionListEmptyEvent: ReceiveChannel<Unit>
        get() = _questionListEmptyEvent

    @OptIn(ExperimentalMaterialApi::class)
    fun setup() {
        workbookListWatchUseCase.setup(scope = viewModelScope)
        folderListWatchUseCase.setup(scope = viewModelScope)

        viewModelScope.launch {
            combine(
                folderListWatchUseCase.flow,
                workbookListWatchUseCase.flow,
            ) { folderListResource, workbookListResource ->
                Resource.merge(
                    folderListResource, workbookListResource
                ) { folderList, workbookList ->
                    WorkbookListResources(
                        folderList = folderList,
                        workbookList = workbookList
                    )
                }
            }
                .onEach {
                    _uiState.value = _uiState.value.copy(
                        resources = it
                    )
                }
                .launchIn(this)

        }
    }

    fun load() =
        viewModelScope.launch {
            workbookListWatchUseCase.load()
            folderListWatchUseCase.load()
        }

    fun updateFolder(folder: FolderUseCaseModel, newFolderName: String) =
        viewModelScope.launch {
            userFolderCommandUseCase.updateFolder(folder, newFolderName)
        }

    fun deleteFolder(folder: FolderUseCaseModel) =
        viewModelScope.launch {
            userFolderCommandUseCase.deleteFolder(folder)
        }

    fun swapFolder(sourceFolderId: Long, destFolderId: Long) =
        viewModelScope.launch {
            userFolderCommandUseCase.swapFolder(sourceFolderId, destFolderId)
        }

    fun onWorkbookClicked(workbook: WorkbookUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                workbookListDrawerState = WorkbookListDrawerState.OperateWorkbook(workbook = workbook)
            )
        }

    fun onSharedWorkbookClicked(sharedWorkbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                workbookListDrawerState = WorkbookListDrawerState.OperateSharedWorkbook(workbook = sharedWorkbook)
            )
        }

    fun onAnswerWorkbookClicked(workbook: WorkbookUseCaseModel) =
        viewModelScope.launch {

            if (workbook.isQuestionListEmpty) {
                _questionListEmptyEvent.send(Unit)
                return@launch
            }

            if (answerSettingGetUseCase.getAnswerSetting().isShowAnswerSettingDialog) {
                _uiState.value = _uiState.value.copy(
                    workbookListDrawerState = WorkbookListDrawerState.AnswerSetting(workbook = workbook)
                )
            } else {
                _navigateToAnswerWorkbookEvent.send(
                    NavigateToAnswerWorkbookArgs(
                        workbookId = workbook.id,
                        isRetry = false
                    )
                )
            }
        }

    fun onStartAnswerClicked(workbookId: Long) =
        viewModelScope.launch {
            _navigateToAnswerWorkbookEvent.send(
                NavigateToAnswerWorkbookArgs(
                    workbookId = workbookId,
                    isRetry = false
                )
            )
        }

    fun deleteWorkbook(workbook: WorkbookUseCaseModel) =
        viewModelScope.launch {
            userWorkbookCommandUseCase.deleteWorkbook(workbook)
        }

    fun swapWorkbook(sourceWorkbookId: Long, destWorkbookId: Long) =
        viewModelScope.launch {
            userWorkbookCommandUseCase.swapWorkbooks(sourceWorkbookId, destWorkbookId)
        }

    private fun getNoFolderWorkbookList(
        workBookList: List<WorkbookUseCaseModel>,
        folderList: List<FolderUseCaseModel>
    ) =
        workBookList.filterNot { workbook ->
            folderList.map { it.name }.contains(workbook.folderName)
        }
}

data class WorkbookListUiState(
    val resources: Resource<WorkbookListResources>,
    val workbookListDrawerState: WorkbookListDrawerState
)

sealed class WorkbookListDrawerState {
    object None : WorkbookListDrawerState()
    data class OperateWorkbook(val workbook: WorkbookUseCaseModel) : WorkbookListDrawerState()
    data class OperateSharedWorkbook(val workbook: SharedWorkbookUseCaseModel) :
        WorkbookListDrawerState()

    data class AnswerSetting(val workbook: WorkbookUseCaseModel) : WorkbookListDrawerState()
}

data class WorkbookListResources(
    val folderList: List<FolderUseCaseModel>,
    val workbookList: List<WorkbookUseCaseModel>,
)

data class NavigateToAnswerWorkbookArgs(
    val workbookId: Long,
    val isRetry: Boolean
)

data class NavigateToAnswerSettingArgs(
    val workbookId: Long,
    val workbookName: String
)
