package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.domain.UploadTestDestination
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UploadWorkbookViewModel(
    auth: Auth,
    private val repository: TestMakerRepository,
    private val logger: TestMakerLogger,
    private val dynamicLinksCreator: DynamicLinksCreator
) : ViewModel() {

    private val _uiState = MutableStateFlow<UploadWorkbookUiState>(UploadWorkbookUiState.Initial)
    val uiState: StateFlow<UploadWorkbookUiState> = _uiState

    init {
        auth.getUser()?.let {
            _uiState.value = UploadWorkbookUiState.Editing
        } ?: run {
            _uiState.value = UploadWorkbookUiState.UnAuthorized
        }
    }

    fun setUser(user: FirebaseUser) {
        _uiState.value = UploadWorkbookUiState.Editing
    }

    fun uploadWorkbook(workbook: Test, comment: String, isPrivate: Boolean) {
        viewModelScope.launch {

            _uiState.value = UploadWorkbookUiState.Loading

            val response = repository.uploadWorkbook(
                test = workbook,
                overview = comment,
                isPublic = !isPrivate
            )

            when (response) {
                is RemoteDataSource.FirebasePostResponse.Success -> {
                    logger.logUploadTestEvent(
                        test = workbook,
                        destination = if (isPrivate) UploadTestDestination.PRIVATE.title else UploadTestDestination.PUBLIC.title
                    )

                    _uiState.value = UploadWorkbookUiState.UploadSuccess(response.documentId)
                }
                is RemoteDataSource.FirebasePostResponse.Divided -> {
                    _uiState.value = UploadWorkbookUiState.UploadDivided
                }
                is RemoteDataSource.FirebasePostResponse.Failure -> {
                    _uiState.value = UploadWorkbookUiState.UploadFailure
                }
            }
        }
    }

    fun createDynamicLinks(documentId: String) {
        viewModelScope.launch {
            _uiState.value = UploadWorkbookUiState.Loading

            dynamicLinksCreator.createShareTestDynamicLinks(documentId).shortLink?.let {
                _uiState.value = UploadWorkbookUiState.CreateDynamicLinksSuccess(it)
            } ?: run {
                _uiState.value = UploadWorkbookUiState.CreateDynamicLinksFailure
            }
        }
    }

    fun reEdit() {
        _uiState.value = UploadWorkbookUiState.Editing
    }
}


sealed class UploadWorkbookUiState {
    object Initial : UploadWorkbookUiState()
    object UnAuthorized : UploadWorkbookUiState()
    object Editing : UploadWorkbookUiState()
    object Loading : UploadWorkbookUiState()
    class UploadSuccess(val documentId: String) : UploadWorkbookUiState()
    object UploadDivided : UploadWorkbookUiState()
    object UploadFailure : UploadWorkbookUiState()
    class CreateDynamicLinksSuccess(val uri: Uri) : UploadWorkbookUiState()
    object CreateDynamicLinksFailure : UploadWorkbookUiState()
}