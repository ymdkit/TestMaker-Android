package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TestMakerRepository, // todo 依存を剥がす
) : ViewModel(), LifecycleObserver {

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

}

data class MainUiState(
    val isImportingWorkbook: Boolean
)
