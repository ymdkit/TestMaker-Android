package com.example.usecase

import com.example.core.utils.Resource
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.FolderUseCaseModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FolderListWatchUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    private val _flow: MutableStateFlow<Resource<List<FolderUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<FolderUseCaseModel>>> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {

            workBookRepository.updateFolderListFlow
                .onEach { list ->
                    val newFolderList = list.map {
                        FolderUseCaseModel.fromFolder(it)
                    }
                    _flow.emit(_flow.value.map {
                        newFolderList
                    })
                }.launchIn(scope)

        }
    }

    suspend fun load(
        folderFilter: (FolderUseCaseModel) -> Boolean = { true }
    ) {
        _flow.emit(Resource.Loading)
        val folderList = workBookRepository.getFolderList()
        _flow.emit(Resource.Success(folderList.map {
            FolderUseCaseModel.fromFolder(it)
        }.filter(folderFilter)))
    }

}

