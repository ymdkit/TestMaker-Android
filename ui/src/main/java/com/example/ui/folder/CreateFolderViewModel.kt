package com.example.ui.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.TestMakerColor
import com.example.usecase.UserFolderCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateFolderViewModel @Inject constructor(
    private val userFolderCommandUseCase: UserFolderCommandUseCase
) : ViewModel() {

    fun createFolder(name: String, color: TestMakerColor) =
        viewModelScope.launch {
            userFolderCommandUseCase.createFolder(name, color)
        }

}
