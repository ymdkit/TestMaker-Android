package com.example.ui.home

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.SharedWorkbookCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sharedWorkbookCommandUseCase: SharedWorkbookCommandUseCase
) : ViewModel(), LifecycleObserver {

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState(
            isDownloading = false
        )
    )
    val uiState: MutableStateFlow<MainUiState>
        get() = _uiState

    private val _downloadWorkbookEvent: Channel<Unit> = Channel()
    val downloadWorkbookEvent: ReceiveChannel<Unit>
        get() = _downloadWorkbookEvent

    fun downloadWorkbook(workbookId: String) =
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDownloading = true
            )
            sharedWorkbookCommandUseCase.downloadWorkbook(workbookId)
            _uiState.value = _uiState.value.copy(
                isDownloading = false
            )
            _downloadWorkbookEvent.send(Unit)
        }
}

data class MainUiState(
    val isDownloading: Boolean
)

