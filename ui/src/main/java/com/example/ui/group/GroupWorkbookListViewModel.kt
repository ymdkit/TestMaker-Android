package com.example.ui.group

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.*
import com.example.usecase.model.GroupUseCaseModel
import com.example.usecase.model.SharedWorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class GroupWorkbookListViewModel @Inject constructor(
    private val groupWorkbookListWatchUseCase: GroupWorkbookListWatchUseCase,
    private val groupCommandUseCase: GroupCommandUseCase,
    private val sharedWorkbookCommandUseCase: SharedWorkbookCommandUseCase,
    private val userWatchUseCase: UserWatchUseCase,
    private val userAuthCommandUseCase: UserAuthCommandUseCase,
    private val groupWatchUseCase: GroupWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<GroupWorkbookListUiState> =
        MutableStateFlow(
            GroupWorkbookListUiState(
                group = Resource.Empty,
                workbookList = Resource.Empty,
                showingMenu = false,
                isOwner = false,
                showingEditGroupDialog = false,
                editingGroupName = "",
                selectedSharedWorkbook = null,
                isRefreshing = true,
                isLogin = false
            )
        )
    val uiState: StateFlow<GroupWorkbookListUiState>
        get() = _uiState

    private val _inviteGroupEvent: Channel<Pair<String, Uri>> = Channel()
    val inviteGroupEvent: ReceiveChannel<Pair<String, Uri>>
        get() = _inviteGroupEvent

    private val _exitGroupEvent: Channel<Unit> = Channel()
    val exitGroupEvent: ReceiveChannel<Unit>
        get() = _exitGroupEvent

    private val _shareWorkbookEvent: Channel<Pair<String, Uri>> = Channel()
    val shareWorkbookEvent: ReceiveChannel<Pair<String, Uri>>
        get() = _shareWorkbookEvent

    private val _downloadWorkbookEvent: Channel<String> = Channel()
    val downloadWorkbookEvent: ReceiveChannel<String>
        get() = _downloadWorkbookEvent

    private lateinit var groupId: String

    @OptIn(FlowPreview::class)
    fun setup(groupId: String) {
        this.groupId = groupId
        groupWorkbookListWatchUseCase.setup(
            groupId = groupId,
            scope = viewModelScope
        )
        userWatchUseCase.setup(scope = viewModelScope)
        groupWatchUseCase.setup(groupId = groupId, scope = viewModelScope)

        groupWorkbookListWatchUseCase.flow
            .debounce(500)
            .onEach {
                _uiState.value = _uiState.value.copy(
                    workbookList = it,
                    isRefreshing = false
                )
            }.launchIn(viewModelScope)

        combine(
            userWatchUseCase.flow,
            groupWatchUseCase.flow
        ) { user, groupResource ->
            user to groupResource
        }.onEach {
            val user = it.first
            val group = it.second
            _uiState.value = _uiState.value.copy(
                group = group,
                isOwner = user != null && group.getOrNull()?.userId == user.id,
                isLogin = user != null
            )
        }.launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true
            )
            groupWorkbookListWatchUseCase.load()
            groupWatchUseCase.load()
        }

    fun onUserCreated() =
        viewModelScope.launch {
            userAuthCommandUseCase.registerUser()
            load()
        }

    fun onInviteButtonClicked() =
        viewModelScope.launch {
            val group = _uiState.value.group.getOrNull() ?: return@launch
            val uri = groupCommandUseCase.inviteGroup(groupId = group.id)
            _inviteGroupEvent.send(group.name to uri)
        }

    fun onMenuToggleButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingMenu = !_uiState.value.showingMenu
            )
        }

    fun onEditGroupButtonClicked() =
        viewModelScope.launch {
            val group = _uiState.value.group.getOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                showingEditGroupDialog = true,
                editingGroupName = group.name
            )
        }


    fun onGroupNameChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editingGroupName = value
            )
        }

    fun onCancelEditGroupButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingEditGroupDialog = false
            )
        }

    fun onUpdateGroup(groupName: String) =
        viewModelScope.launch {
            val group = _uiState.value.group.getOrNull() ?: return@launch
            val newGroup = group.copy(
                name = groupName
            )
            groupCommandUseCase.updateGroup(
                group = newGroup
            )
            _uiState.value = _uiState.value.copy(
                showingEditGroupDialog = false,
                group = Resource.Success(newGroup)
            )
        }

    fun onDeleteGroupButtonClicked() =
        viewModelScope.launch {
            val group = _uiState.value.group.getOrNull() ?: return@launch
            groupCommandUseCase.deleteGroup(group)
            _exitGroupEvent.send(Unit)
        }

    fun onExitGroupButtonClicked() =
        viewModelScope.launch {
            groupCommandUseCase.exitGroup(groupId)
            _exitGroupEvent.send(Unit)
        }

    fun onWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedSharedWorkbook = workbook
            )
        }

    fun onDownloadWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            sharedWorkbookCommandUseCase.downloadWorkbook(documentId = workbook.id)
            _downloadWorkbookEvent.send(workbook.name)
        }

    fun onShareWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            val uri = sharedWorkbookCommandUseCase.shareWorkbook(workbook = workbook)
            _shareWorkbookEvent.send(workbook.name to uri)
        }

    fun onDeleteWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            sharedWorkbookCommandUseCase.deleteWorkbookFromGroup(
                groupId = groupId,
                workbook = workbook
            )
        }
}

data class GroupWorkbookListUiState @OptIn(ExperimentalMaterialApi::class) constructor(
    val group: Resource<GroupUseCaseModel?>,
    val workbookList: Resource<List<SharedWorkbookUseCaseModel>>,
    val showingMenu: Boolean,
    val showingEditGroupDialog: Boolean,
    val editingGroupName: String,
    val isOwner: Boolean,
    val selectedSharedWorkbook: SharedWorkbookUseCaseModel?,
    val isRefreshing: Boolean,
    val isLogin: Boolean,
)