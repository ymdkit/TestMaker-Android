package com.example.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.UserCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateFolderViewModel @Inject constructor(
    private val userCommandUseCase: UserCommandUseCase,
) : ViewModel() {

    fun createFolder(name: String, color: Int) =
        viewModelScope.launch {
            userCommandUseCase.createFolder(name, color)
        }

}
