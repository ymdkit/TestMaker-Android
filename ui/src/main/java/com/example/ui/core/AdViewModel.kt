package com.example.ui.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.IsRemovedAdWatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdViewModel @Inject constructor(
    private val isRemovedAdWatchUseCase: IsRemovedAdWatchUseCase,
) : ViewModel() {

    private val _isRemovedAd: MutableStateFlow<Boolean> =
        MutableStateFlow(isRemovedAdWatchUseCase.get())
    val isRemovedAd: StateFlow<Boolean>
        get() = _isRemovedAd

    fun setup() {
        isRemovedAdWatchUseCase.setup(viewModelScope)

        viewModelScope.launch {
            isRemovedAdWatchUseCase.flow.onEach {
                _isRemovedAd.emit(it)
            }.launchIn(this)
        }
    }
}
