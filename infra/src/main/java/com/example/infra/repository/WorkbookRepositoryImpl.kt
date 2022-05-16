package com.example.infra.repository

import com.example.core.QuestionType
import com.example.core.TestMakerColor
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
import java.util.*
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
        color: TestMakerColor
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

    override suspend fun swapFolder(sourceFolder: Folder, destFolder: Folder) {
        folderDataSource.createFolder(RealmCategory.fromFolder((sourceFolder.copy(order = destFolder.order))))
        folderDataSource.createFolder(RealmCategory.fromFolder((destFolder.copy(order = sourceFolder.order))))
    }

    override suspend fun getWorkbookList(): List<Workbook> {
        val folderNameList = folderDataSource.getFolderList().map { it.name }
        return workbookDataSource.getWorkbookList().map {
            it.toWorkbook(folderNameList)
        }
    }

    override suspend fun getWorkbook(workbookId: WorkbookId): Workbook {
        val folderNameList = folderDataSource.getFolderList().map { it.name }
        return workbookDataSource.getWorkbook(id = workbookId.value).toWorkbook(folderNameList)
    }

    override suspend fun createWorkbook(
        name: String,
        remoteId: String,
        color: TestMakerColor,
        folderName: String
    ): Workbook {
        val workbookId = workbookDataSource.generateWorkbookId()
        val newWorkbook = Workbook(
            id = WorkbookId(workbookId),
            remoteId = remoteId,
            name = name,
            color = color,
            folderName = folderName,
            order = workbookId.toInt(),
            questionList = listOf()
        )
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(newWorkbook))
        refreshWorkbookList()
        refreshFolderList()
        return newWorkbook
    }

    override suspend fun updateWorkbook(workbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(workbook))
        refreshWorkbookList()
    }

    override suspend fun deleteWorkbook(workbookId: WorkbookId) {
        workbookDataSource.deleteWorkbook(workbookId.value)
        refreshWorkbookList()
        refreshFolderList()
    }

    override suspend fun exportWorkbook(workbook: Workbook) =
        ExportedWorkbook(
            value = cloudFunctionsApi.testToText(
                workbook = ExportWorkbookRequest.fromWorkbook(
                    workbook
                )
            ).text
        )

    override suspend fun importWorkbook(
        workbookName: String,
        exportedWorkbook: ExportedWorkbook,
    ): Workbook {
        val importedWorkbook = cloudFunctionsApi.textToTest(
            workbookName = workbookName,
            text = exportedWorkbook.value,
            lang = if (Locale.getDefault().language == "ja") "ja" else "en"
        )

        val newQuestionId = workbookDataSource.generateQuestionId()
        val newQuestionList = importedWorkbook.questions.mapIndexed { index, it ->

            val questionType = QuestionType.valueOf(it.type)
            val newAnswerList = when (questionType) {
                QuestionType.WRITE -> listOf(it.answer)
                QuestionType.SELECT -> listOf(it.answer)
                QuestionType.COMPLETE -> it.answers
                QuestionType.SELECT_COMPLETE -> it.answers
            }

            Quest.fromCreateQuestionRequest(
                questionId = newQuestionId + index,
                request = CreateQuestionRequest(
                    questionType = questionType,
                    problem = it.question,
                    answers = newAnswerList,
                    explanation = it.explanation,
                    problemImageUrl = it.imagePath,
                    explanationImageUrl = "", // todo
                    otherSelections = it.others,
                    isAutoGenerateOtherSelections = it.isAutoGenerateOthers,
                    isCheckAnswerOrder = it.isCheckOrder
                ),
            )
        }

        val newWorkbookId = workbookDataSource.generateWorkbookId()
        val newWorkbook = Workbook(
            id = WorkbookId(value = newWorkbookId),
            remoteId = "",
            name = importedWorkbook.title,
            color = TestMakerColor.BLUE,
            order = newWorkbookId.toInt(),
            folderName = "",
            questionList = newQuestionList.map { it.toQuestion() }
        )

        workbookDataSource.createQuestions(questionList = newQuestionList)
        workbookDataSource.createWorkbook(workbook = RealmTest.fromWorkbook(newWorkbook))
        refreshWorkbookList()

        return newWorkbook
    }

    override suspend fun swapWorkbook(sourceWorkbook: Workbook, destWorkbook: Workbook) {
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(sourceWorkbook.copy(order = destWorkbook.order)))
        workbookDataSource.createWorkbook(RealmTest.fromWorkbook(destWorkbook.copy(order = sourceWorkbook.order)))
    }

    override suspend fun getWorkbookListByFolderName(folderName: String): List<Workbook> {
        val folderNameList = folderDataSource.getFolderList().map { it.name }
        return workbookDataSource.getWorkbookList()
            .filter { workbook -> workbook.getCategory() == folderName }
            .map { it.toWorkbook(folderNameList) }
    }

    override suspend fun getQuestion(questionId: QuestionId): Question =
        workbookDataSource.getQuestion(questionId.value).toQuestion()

    override suspend fun createQuestion(
        workbookId: WorkbookId,
        request: CreateQuestionRequest
    ) {
        val folderNameList = folderDataSource.getFolderList().map { it.name }
        val newQuestionId = workbookDataSource.generateQuestionId()
        val newQuestion = Quest.fromCreateQuestionRequest(
            questionId = newQuestionId,
            request = request
        )

        workbookDataSource.createQuestions(questionList = listOf(newQuestion))
        val workbook = workbookDataSource.getWorkbook(workbookId.value).toWorkbook(folderNameList)
        workbookDataSource.createWorkbook(
            RealmTest.fromWorkbook(workbook.copy(questionList = workbook.questionList + newQuestion.toQuestion()))
        )
        refreshWorkbookList()
    }

    override suspend fun createQuestionList(
        workbookId: WorkbookId,
        requestList: List<CreateQuestionRequest>
    ) {
        val folderNameList = folderDataSource.getFolderList().map { it.name }
        val newQuestionId = workbookDataSource.generateQuestionId()
        val newQuestionList = requestList.mapIndexed { index, it ->

            val questionType = it.questionType

            Quest.fromCreateQuestionRequest(
                questionId = newQuestionId + index,
                request = CreateQuestionRequest(
                    questionType = questionType,
                    problem = it.problem,
                    answers = it.answers,
                    explanation = it.explanation,
                    problemImageUrl = it.problemImageUrl,
                    explanationImageUrl = it.explanationImageUrl,
                    otherSelections = it.otherSelections,
                    isAutoGenerateOtherSelections = it.isAutoGenerateOtherSelections,
                    isCheckAnswerOrder = it.isCheckAnswerOrder
                ),
            )
        }
        workbookDataSource.createQuestions(newQuestionList)

        val workbook = workbookDataSource.getWorkbook(workbookId.value).toWorkbook(folderNameList)
        workbookDataSource.createWorkbook(
            RealmTest.fromWorkbook(workbook.copy(questionList = workbook.questionList + newQuestionList.map { it.toQuestion() }))
        )
        refreshWorkbookList()
    }

    override suspend fun updateQuestion(question: Question) {
        workbookDataSource.updateQuestion(Quest.fromQuestion(question))
        _updateWorkbookListFlow.emit(getWorkbookList())
    }

    override suspend fun swapQuestion(sourceQuestion: Question, destQuestion: Question) {
        workbookDataSource.updateQuestion(Quest.fromQuestion(sourceQuestion.copy(order = destQuestion.order)))
        workbookDataSource.updateQuestion(Quest.fromQuestion(destQuestion.copy(order = sourceQuestion.order)))
    }

    private suspend fun refreshWorkbookList() {
        _updateWorkbookListFlow.emit(getWorkbookList())
    }

    private suspend fun refreshFolderList() {
        _updateFolderListFlow.emit(getFolderList())
    }
}