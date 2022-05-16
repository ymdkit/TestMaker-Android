package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.*
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val workbookListWatchUseCase: WorkbookListWatchUseCase,
    private val folderListWatchUseCase: FolderListWatchUseCase,
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val userFolderCommandUseCase: UserFolderCommandUseCase,
    private val answerSettingGetUseCase: AnswerSettingWatchUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<Resource<UiState>> =
        MutableStateFlow(Resource.Empty)
    val uiState: StateFlow<Resource<UiState>>
        get() = _uiState

    private val _navigateToAnswerSettingEvent: Channel<NavigateToAnswerSettingArgs> = Channel()
    val navigateToAnswerSettingEvent: ReceiveChannel<NavigateToAnswerSettingArgs>
        get() = _navigateToAnswerSettingEvent

    private val _navigateToAnswerWorkbookEvent: Channel<NavigateToAnswerWorkbookArgs> = Channel()
    val navigateToAnswerWorkbookEvent: ReceiveChannel<NavigateToAnswerWorkbookArgs>
        get() = _navigateToAnswerWorkbookEvent

    private val _questionListEmptyEvent: Channel<Unit> = Channel()
    val questionListEmptyEvent: ReceiveChannel<Unit>
        get() = _questionListEmptyEvent

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
            userFolderCommandUseCase.updateFolder(folder)
        }

    fun deleteFolder(folder: FolderUseCaseModel) =
        viewModelScope.launch {
            userFolderCommandUseCase.deleteFolder(folder)
        }

    fun swapFolder(sourceFolderId: Long, destFolderId: Long) =
        viewModelScope.launch {
            userFolderCommandUseCase.swapFolder(sourceFolderId, destFolderId)
        }

    fun onAnswerWorkbookClicked(workbook: WorkbookUseCaseModel) =
        viewModelScope.launch {

            if (workbook.isQuestionListEmpty) {
                _questionListEmptyEvent.send(Unit)
                return@launch
            }

            if (answerSettingGetUseCase.getAnswerSetting().isShowAnswerSettingDialog) {
                _navigateToAnswerSettingEvent.send(
                    NavigateToAnswerSettingArgs(
                        workbookId = workbook.id,
                        workbookName = workbook.name
                    )
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

data class UiState(
    val folderList: List<FolderUseCaseModel>,
    val workBookList: List<WorkbookUseCaseModel>
)

data class NavigateToAnswerWorkbookArgs(
    val workbookId: Long,
    val isRetry: Boolean
)

data class NavigateToAnswerSettingArgs(
    val workbookId: Long,
    val workbookName: String
)
