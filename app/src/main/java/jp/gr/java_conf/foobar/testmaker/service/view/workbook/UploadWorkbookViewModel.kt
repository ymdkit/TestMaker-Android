package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.SharedWorkbookCommandUseCase
import com.example.usecase.UserAuthCommandUseCase
import com.example.usecase.UserWatchUseCase
import com.example.usecase.WorkbookListWatchUseCase
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
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
class UploadWorkbookViewModel @Inject constructor(
    private val workbookListWatchUseCase: WorkbookListWatchUseCase,
    private val userWatchUseCase: UserWatchUseCase,
    private val sharedWorkbookCommandUseCase: SharedWorkbookCommandUseCase,
    private val userAuthCommandUseCase: UserAuthCommandUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        UploadWorkbookUiState(
            workbookList = Resource.Empty,
            selectedWorkbook = null,
            isLogin = false,
            isPrivateUpload = false,
            comment = "",
            showingDropDownMenu = false,
            isUploading = false
        )
    )
    val uiState: StateFlow<UploadWorkbookUiState> = _uiState

    private val _uploadWorkbookEvent: Channel<Unit> = Channel()
    val uploadWorkbookEvent: ReceiveChannel<Unit>
        get() = _uploadWorkbookEvent

    private lateinit var groupId: String

    fun setup(groupId: String, isPrivateUpload: Boolean) {
        this.groupId = groupId

        _uiState.value = _uiState.value.copy(
            isPrivateUpload = isPrivateUpload
        )

        workbookListWatchUseCase.setup(viewModelScope)
        userWatchUseCase.setup(viewModelScope)

        workbookListWatchUseCase.flow
            .onEach {
                val newWorkbookList = it.map { it.filter { it.questionCount > 0 } }
                _uiState.value = _uiState.value.copy(
                    workbookList = newWorkbookList,
                    selectedWorkbook = newWorkbookList.getOrNull()?.firstOrNull()
                )
            }
            .launchIn(viewModelScope)

        userWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    isLogin = it != null
                )
            }
            .launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            workbookListWatchUseCase.load()
        }

    fun onUserCreated() =
        viewModelScope.launch {
            userAuthCommandUseCase.registerUser()
        }

    fun onToggleDropDownMenu() = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            showingDropDownMenu = !_uiState.value.showingDropDownMenu
        )
    }

    fun onWorkbookSelected(workbook: WorkbookUseCaseModel) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            selectedWorkbook = workbook
        )
    }

    fun onCommentChanged(value: String) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            comment = value
        )
    }

    fun onIsPrivateUploadChanged(value: Boolean) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isPrivateUpload = value
        )
    }

    fun uploadWorkbook() = viewModelScope.launch {
        val workbook = _uiState.value.selectedWorkbook ?: return@launch
        _uiState.value = _uiState.value.copy(
            isUploading = true
        )
        sharedWorkbookCommandUseCase.uploadWorkbook(
            groupId = groupId,
            isPublic = !_uiState.value.isPrivateUpload,
            comment = _uiState.value.comment,
            workbook = workbook,
        )
        _uiState.value = _uiState.value.copy(
            isUploading = false
        )
        _uploadWorkbookEvent.send(Unit)
    }
}

data class UploadWorkbookUiState(
    val workbookList: Resource<List<WorkbookUseCaseModel>>,
    val selectedWorkbook: WorkbookUseCaseModel?,
    val isLogin: Boolean,
    val isPrivateUpload: Boolean,
    val comment: String,
    val showingDropDownMenu: Boolean,
    val isUploading: Boolean
)