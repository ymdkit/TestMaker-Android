package com.example.usecase

import com.example.core.TestMakerColor
import com.example.domain.model.CreateQuestionRequest
import com.example.domain.model.DocumentId
import com.example.domain.model.GroupId
import com.example.domain.repository.SharedWorkbookRepository
import com.example.domain.repository.UserRepository
import com.example.domain.repository.WorkBookRepository
import com.example.usecase.model.SharedWorkbookUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedWorkbookCommandUseCase @Inject constructor(
    private val repository: SharedWorkbookRepository,
    private val workbookRepository: WorkBookRepository,
    private val userRepository: UserRepository,
) {

    suspend fun uploadWorkbook(
        groupId: String,
        isPublic: Boolean,
        comment: String,
        workbook: WorkbookUseCaseModel
    ) {
        val user = userRepository.getUserOrNull() ?: return

        try {
            repository.createWorkbook(
                user = user,
                groupId = GroupId(groupId),
                isPublic = isPublic,
                workbook = workbook.toWorkbook(),
                comment = comment,
            )
        } catch (e: Exception) {
            println("upload error: ${e.message}")
        }
    }

    suspend fun shareWorkbook(workbook: SharedWorkbookUseCaseModel) =
        repository.shareWorkbook(DocumentId(workbook.id))

    suspend fun deleteWorkbook(workbook: SharedWorkbookUseCaseModel) {
        val user = userRepository.getUserOrNull() ?: return
        if (workbook.userId != user.id.value) return
        repository.deleteWorkbook(userId = user.id, DocumentId(workbook.id))
    }

    suspend fun deleteWorkbookFromGroup(groupId: String, workbook: SharedWorkbookUseCaseModel) {
        val user = userRepository.getUserOrNull() ?: return
        if (workbook.userId != user.id.value) return
        repository.deleteWorkbookFromGroup(groupId = GroupId(groupId), DocumentId(workbook.id))
    }

    suspend fun downloadWorkbook(documentId: String) {
        val questionList =
            repository.getQuestionListByWorkbookId(documentId = DocumentId(documentId))
        val workbook = repository.findWorkbookById(documentId = DocumentId(documentId)) ?: return

        val newWorkbook = workbookRepository.createWorkbook(
            name = workbook.name,
            remoteId = workbook.id.value,
            // todo
            color = TestMakerColor.BLUE,
            folderName = ""
        )
        questionList.forEach {
            workbookRepository.createQuestion(
                workbookId = newWorkbook.id,
                request = CreateQuestionRequest.fromSharedQuestion(it)
            )
        }
    }
}

