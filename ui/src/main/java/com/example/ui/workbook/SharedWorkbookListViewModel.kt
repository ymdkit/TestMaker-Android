package com.example.ui.workbook

import android.net.Uri
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.utils.Resource
import com.example.usecase.SharedOwnWorkbookListWatchUseCase
import com.example.usecase.SharedWorkbookCommandUseCase
import com.example.usecase.model.SharedWorkbookUseCaseModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class MyWorkbookListViewModel @Inject constructor(
    private val sharedOwnWorkbookListWatchUseCase: SharedOwnWorkbookListWatchUseCase,
    private val sharedWorkbookCommandUseCase: SharedWorkbookCommandUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyWorkbookListUiState> =
        MutableStateFlow(
            MyWorkbookListUiState(
                myWorkbookList = Resource.Empty,
                selectedSharedWorkbook = null,
                isDownloading = false
            )
        )
    val uiState: StateFlow<MyWorkbookListUiState>
        get() = _uiState

    private val _shareWorkbookEvent: Channel<Pair<String, Uri>> = Channel()
    val shareWorkbookEvent: ReceiveChannel<Pair<String, Uri>>
        get() = _shareWorkbookEvent

    private val _downloadWorkbookEvent: Channel<String> = Channel()
    val downloadWorkbookEvent: ReceiveChannel<String>
        get() = _downloadWorkbookEvent

    @OptIn(ExperimentalMaterialApi::class)
    fun setup() {
        sharedOwnWorkbookListWatchUseCase.setup(scope = viewModelScope)

        viewModelScope.launch {

            sharedOwnWorkbookListWatchUseCase.flow
                .onEach {
                    _uiState.value = _uiState.value.copy(
                        myWorkbookList = it
                    )
                }
                .launchIn(this)
        }
    }

    fun load() =
        viewModelScope.launch {
            sharedOwnWorkbookListWatchUseCase.load()
        }

    fun onWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedSharedWorkbook = workbook
            )
        }

    fun onDownloadWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true
            )
            sharedWorkbookCommandUseCase.downloadWorkbook(documentId = workbook.id)
            _downloadWorkbookEvent.send(workbook.name)
            _uiState.value = _uiState.value.copy(
                isDownloading = false
            )
        }

    fun onShareWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            val uri = sharedWorkbookCommandUseCase.shareWorkbook(workbook = workbook)
            _shareWorkbookEvent.send(workbook.name to uri)
        }

    fun onDeleteWorkbookClicked(workbook: SharedWorkbookUseCaseModel) =
        viewModelScope.launch {
            sharedWorkbookCommandUseCase.deleteWorkbook(
                workbook = workbook
            )
        }

}

data class MyWorkbookListUiState(
    val myWorkbookList: Resource<List<SharedWorkbookUseCaseModel>>,
    val selectedSharedWorkbook: SharedWorkbookUseCaseModel?,
    val isDownloading: Boolean
)
