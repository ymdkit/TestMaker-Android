package com.example.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.GroupCommandUseCase
import com.example.usecase.GroupListWatchUseCase
import com.example.usecase.model.GroupUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupListViewModel @Inject constructor(
    private val groupListWatchUseCase: GroupListWatchUseCase,
    private val groupCommandUseCase: GroupCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<GroupListUiState> =
        MutableStateFlow(
            GroupListUiState(
                groupList = Resource.Empty,
                showingCreateGroupDialog = false,
                editingGroupName = ""
            )
        )
    val uiState: StateFlow<GroupListUiState>
        get() = _uiState


    fun setup() {
        groupListWatchUseCase.setup(
            scope = viewModelScope
        )

        groupListWatchUseCase.flow
            .onEach {
                _uiState.value = _uiState.value.copy(
                    groupList = it
                )
            }.launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            groupListWatchUseCase.load()
        }

    fun onCreateGroupButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingCreateGroupDialog = true
            )
        }

    fun onGroupNameChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                editingGroupName = value
            )
        }

    fun onCancelCreateGroupButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                showingCreateGroupDialog = false
            )
        }

    fun onCreateGroup(groupName: String) =
        viewModelScope.launch {
            groupCommandUseCase.createGroup(groupName = groupName)
            _uiState.value = _uiState.value.copy(
                showingCreateGroupDialog = false
            )
        }
}

data class GroupListUiState(
    val groupList: Resource<List<GroupUseCaseModel>>,
    val showingCreateGroupDialog: Boolean,
    val editingGroupName: String
)