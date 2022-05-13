package com.example.ui.group

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.GroupCommandUseCase
import com.example.usecase.GroupWorkbookListWatchUseCase
import com.example.usecase.UserWatchUseCase
import com.example.usecase.model.GroupUseCaseModel
import com.example.usecase.model.SharedWorkbookUseCaseModel
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
class GroupWorkbookListViewModel @Inject constructor(
    private val groupWorkbookListWatchUseCase: GroupWorkbookListWatchUseCase,
    private val groupCommandUseCase: GroupCommandUseCase,
    private val userWatchUseCase: UserWatchUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<GroupWorkbookListUiState> =
        MutableStateFlow(
            GroupWorkbookListUiState(
                workbookList = Resource.Empty,
                showingMenu = false,
                isOwner = false,
                showingEditGroupDialog = false,
                editingGroupName = "",
                showingDeleteGroupDialog = false,
                showingExitGroupDialog = false,
            )
        )
    val uiState: StateFlow<GroupWorkbookListUiState>
        get() = _uiState

    private val _inviteGroupEvent: Channel<Pair<String, Uri>> = Channel()
    val inviteGroupEvent: ReceiveChannel<Pair<String, Uri>>
        get() = _inviteGroupEvent

    private lateinit var group: GroupUseCaseModel

    fun setup(group: GroupUseCaseModel) {
        this.group = group
        groupWorkbookListWatchUseCase.setup(
            groupId = group.id,
            scope = viewModelScope
        )
        userWatchUseCase.setup(scope = viewModelScope)

        groupWorkbookListWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    workbookList = it
                )
            }.launchIn(viewModelScope)

        userWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    isOwner = it != null && group.userId == it.id
                )
            }
            .launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            groupWorkbookListWatchUseCase.load()
        }

    fun onInviteButtonClicked() =
        viewModelScope.launch {
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
            val newGroup = group.copy(
                name = groupName
            )
            groupCommandUseCase.updateGroup(
                group = newGroup
            )
            _uiState.value = _uiState.value.copy(
                showingEditGroupDialog = false
            )
            group = newGroup
        }

    fun onDeleteGroupButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingDeleteGroupDialog = true
            )
        }

    fun onExitGroupButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingExitGroupDialog = true
            )
        }
}

data class GroupWorkbookListUiState(
    val workbookList: Resource<List<SharedWorkbookUseCaseModel>>,
    val showingMenu: Boolean,
    val showingEditGroupDialog: Boolean,
    val editingGroupName: String,
    val showingDeleteGroupDialog: Boolean,
    val showingExitGroupDialog: Boolean,
    val isOwner: Boolean,
)