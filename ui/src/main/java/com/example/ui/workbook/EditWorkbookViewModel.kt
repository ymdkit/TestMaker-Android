package com.example.ui.workbook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.TestMakerColor
import com.example.usecase.FolderListWatchUseCase
import com.example.usecase.UserWorkbookCommandUseCase
import com.example.usecase.WorkbookWatchUseCase
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

@HiltViewModel
class EditWorkbookViewModel @Inject constructor(
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
    private val folderListWatchUseCase: FolderListWatchUseCase,
    private val workbookWatchUseCase: WorkbookWatchUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<Resource<UiState>> =
        MutableStateFlow(Resource.Empty)
    val uiState: StateFlow<Resource<UiState>>
        get() = _uiState

    private var workbookId by Delegates.notNull<Long>()

    fun setup(workbookId: Long) {
        this.workbookId = workbookId
        folderListWatchUseCase.setup(scope = viewModelScope)
        workbookWatchUseCase.setup(
            workbookId = workbookId,
            scope = viewModelScope
        )

        combine(
            workbookWatchUseCase.flow,
            folderListWatchUseCase.flow
        ) { workbookResource, folderListResource ->
            Resource.merge(workbookResource, folderListResource) { workbook, folderList ->
                UiState(
                    workbook = workbook,
                    folderList = folderList
                )
            }
        }.onEach {
            _uiState.value = it
        }
            .launchIn(viewModelScope)

    }

    fun load() = viewModelScope.launch {
        folderListWatchUseCase.load()
        workbookWatchUseCase.load()
    }

    fun updateWorkbook(name: String, color: TestMakerColor, folderName: String) =
        viewModelScope.launch {
            userWorkbookCommandUseCase.updateWorkbook(
                workbookId = workbookId,
                name = name,
                color = color,
                folderName = folderName
            )
        }

}

data class UiState(
    val workbook: WorkbookUseCaseModel,
    val folderList: List<FolderUseCaseModel>
)
