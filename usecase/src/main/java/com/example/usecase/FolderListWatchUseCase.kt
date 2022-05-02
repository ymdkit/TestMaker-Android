package com.example.usecase

import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.FolderUseCaseModel
import com.example.usecase.utils.Resource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class FolderListWatchUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    private val _flow: MutableStateFlow<Resource<List<FolderUseCaseModel>>> =
        MutableStateFlow(Resource.Empty)
    val flow: StateFlow<Resource<List<FolderUseCaseModel>>> = _flow

    fun setup(scope: CoroutineScope) {
        scope.launch {

            workBookRepository.createFolderFlow
                .onEach {
                    val new = _flow.value.map { list ->
                        list + FolderUseCaseModel.fromFolder(it)
                    }
                    _flow.emit(new)
                }.launchIn(scope)

            workBookRepository.updateFolderFlow
                .onEach { folder ->
                    val new = _flow.value.map { list ->
                        list.map {
                            if (folder.id.value == it.id) FolderUseCaseModel.fromFolder(
                                folder
                            ) else it
                        }
                    }
                    _flow.emit(new)
                }.launchIn(scope)

            workBookRepository.deleteFolderFlow
                .onEach { folderId ->
                    val new = _flow.value.map { list ->
                        list.filter { it.id != folderId.value }
                    }
                    _flow.emit(new)
                }.launchIn(scope)

        }
    }

    suspend fun load() {
        _flow.emit(Resource.Loading)
        val folderList = workBookRepository.getFolderList()
        _flow.emit(Resource.Success(folderList.map {
            FolderUseCaseModel.fromFolder(it)
        }))
    }

}

