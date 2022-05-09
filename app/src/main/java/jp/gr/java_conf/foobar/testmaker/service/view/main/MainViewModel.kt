package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.usecase.UserWorkbookCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TestMakerRepository, // todo 依存を剥がす
    private val userWorkbookCommandUseCase: UserWorkbookCommandUseCase,
) : ViewModel(), LifecycleObserver {

    private val _importWorkbookCompletionEvent: Channel<String> = Channel()
    val importWorkbookCompletionEvent: ReceiveChannel<String>
        get() = _importWorkbookCompletionEvent

    private val _uiState: MutableStateFlow<MainUiState> = MutableStateFlow(
        MainUiState(
            isImportingWorkbook = false
        )
    )
    val uiState: MutableStateFlow<MainUiState>
        get() = _uiState

    suspend fun downloadTest(testId: String): FirebaseTest = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) =
        repository.createObjectFromFirebase(test, source = CreateTestSource.DYNAMIC_LINKS.title)

    fun importWorkbook(
        workbookName: String,
        exportedWorkbook: String,
    ) = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isImportingWorkbook = true)
        val workbook = userWorkbookCommandUseCase.importWorkbook(
            workbookName = workbookName,
            exportedWorkbook = exportedWorkbook,
        )
        _uiState.value = _uiState.value.copy(isImportingWorkbook = false)
        _importWorkbookCompletionEvent.send(workbook.name)
    }
}

data class MainUiState(
    val isImportingWorkbook: Boolean
)
