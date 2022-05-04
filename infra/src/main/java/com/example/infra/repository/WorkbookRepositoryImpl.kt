package com.example.infra.repository

import com.example.domain.model.*
import com.example.domain.repository.WorkBookRepository
import com.example.infra.local.db.FolderDataSource
import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.local.entity.Quest
import com.example.infra.local.entity.RealmCategory
import com.example.infra.local.entity.RealmTest
import com.example.infra.remote.CloudFunctionsApi
import com.example.infra.remote.CloudFunctionsClient
import com.example.infra.remote.ExportWorkbookRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkbookRepositoryImpl @Inject constructor(
    private val workbookDataSource: WorkbookDataSource,
    private val folderDataSource: FolderDataSource,
    @CloudFunctionsClient
    private val cloudFunctionsApi: CloudFunctionsApi,
) : WorkBookRepository {

    private val _updateFolderListFlow: MutableSharedFlow<List<Folder>> =
        MutableSharedFlow()
    override val updateFolderListFlow: Flow<List<Folder>>
        get() = _updateFolderListFlow

    private val _updateWorkbookListFlow: MutableSharedFlow<List<Workbook>> =
        MutableSharedFlow()
    override val updateWorkBookListFlow: Flow<List<Workbook>>
        get() = _updateWorkbookListFlow

    override suspend fun getFolderList(): List<Folder> {
        return folderDataSource.getFolderList().map {
            val workbookCount = getWorkbookListByFolderName(folderName = it.name).count()
            it.toFolder(workbookCount = workbookCount)
        }
    }

    override suspend fun getFolder(folderId: FolderId): Folder {
        val realmFolder = folderDataSource.getFolder(folderId = folderId.value)
        val workbookCount = getWorkbookListByFolderName(folderName = realmFolder.name).count()
        return realmFolder.toFolder(workbookCount = workbookCount)
    }

    override suspend fun createFolder(
        name: String,
        color: Int
    ) {
        val folderId = folderDataSource.generateFolderId()
        val newFolder = Folder(
            id = FolderId(folderId),
            name = name,
            color = color,
            order = folderId.toInt(),
            workbookCount = 0
        )
        folderDataSource.createFolder(RealmCategory.fromFolder(newFolder))
        refreshFolderList()
    }

    override suspend fun updateFolder(folder: Folder) {
        folderDataSource.createFolder(RealmCategory.fromFolder(folder))
        refreshFolderList()
    }

    override suspend fun deleteFolder(folderId: FolderId) {
        folderDataSource.deleteFolder(folderId.value)
        refreshFolderList()
    }

    override suspend fun getWorkbookList(): List<Workbook> =
        workbookDataSource.getWorkbookList().map {
            it.toWorkbook()
        }

    override suspend fun getWorkbook(workbookId: WorkbookId): Workbook =
        workbookDataSource.getWorkbook(id = workbookId.value).toWorkbook()

    override suspend fun createWorkbook(
        name: String,
        color: Int,
        folderName: String
    ) {
        val workbookId = workbookDataSource.generateWorkbookId()
        val newWorkbook = Workbook(
            id = WorkbookId(workbookId),
            name = name,
            color = color,
            folderName = folderName,
            order = workbookId.toInt(),
            questionList = listOf()
        )
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(newWorkbook))
        refreshWorkbookList()
    }

    override suspend fun updateWorkbook(workbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(workbook))
        refreshWorkbookList()
    }

    override suspend fun deleteWorkbook(workbookId: WorkbookId) {
        workbookDataSource.deleteWorkbook(workbookId.value)
        refreshWorkbookList()
    }

    override suspend fun exportWorkbook(workbook: Workbook) =
        ExportedWorkbook(
            value = cloudFunctionsApi.testToText(
                workbook = ExportWorkbookRequest.fromWorkbook(
                    workbook
                )
            ).text
        )

    override suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(sourceWorkbook.copy(order = destWorkbook.order)))
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(destWorkbook.copy(order = sourceWorkbook.order)))
        refreshWorkbookList()
    }

    override suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook> =
        workbookDataSource.getWorkbookList()
            .filter { workbook -> workbook.getCategory() == folderName }
            .map { it.toWorkbook() }

    override suspend fun getQuestion(questionId: QuestionId): Question =
        workbookDataSource.getQuestion(questionId.value).toQuestion()

    override suspend fun createQuestion(
        workbookId: WorkbookId,
        request: CreateQuestionRequest
    ) {
        val newQuestionId = workbookDataSource.generateQuestionId()
        val newQuestion = Quest.fromCreateQuestionRequest(
            questionId = newQuestionId,
            request = request
        )

        workbookDataSource.createQuestions(questionList = listOf(newQuestion))
        val workbook = workbookDataSource.getWorkbook(workbookId.value).toWorkbook()
        workbookDataSource.createWorkbook(
            RealmTest.fromWorkbook(workbook.copy(questionList = workbook.questionList + newQuestion.toQuestion()))
        )
        refreshWorkbookList()
    }

    override suspend fun updateQuestion(workbookId: WorkbookId, question: Question) {
        workbookDataSource.updateQuestion(Quest.fromQuestion(question))
        refreshWorkbookList()
    }

    private suspend fun refreshWorkbookList() {
        _updateWorkbookListFlow.emit(getWorkbookList())
    }

    private suspend fun refreshFolderList() {
        _updateFolderListFlow.emit(getFolderList())
    }
}