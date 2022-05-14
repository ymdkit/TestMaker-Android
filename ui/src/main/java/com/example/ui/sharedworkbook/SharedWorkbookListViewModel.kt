package com.example.ui.sharedworkbook

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.SharedWorkbookCommandUseCase
import com.example.usecase.SharedWorkbookListWatchUseCase
import com.example.usecase.UserWatchUseCase
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
class SharedWorkbookListViewModel @Inject constructor(
    private val sharedWorkbookCommandUseCase: SharedWorkbookCommandUseCase,
    private val sharedWorkbookListWatchUseCase: SharedWorkbookListWatchUseCase,
    private val userWatchUseCase: UserWatchUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<GroupWorkbookListUiState> =
        MutableStateFlow(
            GroupWorkbookListUiState(
                workbookList = Resource.Empty,
                query = "",
                selectedSharedWorkbook = null,
                isRefreshing = true,
                isSearching = false
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


    @OptIn(FlowPreview::class)
    fun setup() {
        userWatchUseCase.setup(scope = viewModelScope)
        sharedWorkbookListWatchUseCase.setup(scope = viewModelScope)

        sharedWorkbookListWatchUseCase.flow
            .debounce(500)
            .onEach {
                _uiState.value = _uiState.value.copy(
                    workbookList = it,
                    isRefreshing = false
                )
            }.launchIn(viewModelScope)
    }

    fun load() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true
            )
            sharedWorkbookListWatchUseCase.load(query = _uiState.value.query)
        }

    fun onSearchButtonClicked() =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSearching = !_uiState.value.isSearching
            )
        }

    fun onQueryChanged(value: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                query = value
            )
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

}

data class GroupWorkbookListUiState @OptIn(ExperimentalMaterialApi::class) constructor(
    val workbookList: Resource<List<SharedWorkbookUseCaseModel>>,
    val query: String,
    val selectedSharedWorkbook: SharedWorkbookUseCaseModel?,
    val isRefreshing: Boolean,
    val isSearching: Boolean,
)