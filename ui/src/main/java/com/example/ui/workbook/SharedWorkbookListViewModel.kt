package com.example.ui.workbook

import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.SharedWorkbookListWatchUseCase
import com.example.usecase.model.SharedWorkbookUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterialApi::class)
@HiltViewModel
class MyWorkbookListViewModel @Inject constructor(
    private val sharedWorkbookListWatchUseCase: SharedWorkbookListWatchUseCase,
) : ViewModel() {

    private val _uiState: MutableStateFlow<MyWorkbookListUiState> =
        MutableStateFlow(
            MyWorkbookListUiState(
                myWorkbookList = Resource.Empty
            )
        )
    val uiState: StateFlow<MyWorkbookListUiState>
        get() = _uiState

    @OptIn(ExperimentalMaterialApi::class)
    fun setup() {
        sharedWorkbookListWatchUseCase.setup(scope = viewModelScope)

        viewModelScope.launch {

            sharedWorkbookListWatchUseCase.flow
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
            sharedWorkbookListWatchUseCase.load()
        }

}

data class MyWorkbookListUiState(
    val myWorkbookList: Resource<List<SharedWorkbookUseCaseModel>>
)
