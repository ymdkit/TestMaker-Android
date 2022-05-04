package com.example.usecase

import com.example.domain.model.CreateQuestionRequest
import com.example.domain.model.QuestionId
import com.example.domain.model.WorkbookId
import com.example.domain.repository.WorkBookRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserQuestionCommandUseCase @Inject constructor(
    private val workBookRepository: WorkBookRepository
) {

    suspend fun deleteQuestions(workbookId: Long, questionIdList: List<Long>) {
        val workbook = workBookRepository.getWorkbook(WorkbookId(workbookId))
        val newQuestionList =
            workbook.questionList.filterNot { questionIdList.contains(it.id.value) }
        workBookRepository.updateWorkbook(workbook.copy(questionList = newQuestionList))
    }

    suspend fun swapQuestions(sourceQuestionId: Long, destQuestionId: Long) {
        val sourceQuestion =
            workBookRepository.getQuestion(questionId = QuestionId(sourceQuestionId))
        val destQuestion = workBookRepository.getQuestion(questionId = QuestionId(destQuestionId))
        workBookRepository.updateQuestion(
            question = sourceQuestion.copy(order = destQuestion.order)
        )
        workBookRepository.updateQuestion(
            question = destQuestion.copy(order = sourceQuestion.order)
        )
    }

    suspend fun moveQuestionsToOtherWorkbook(
        sourceWorkbookId: Long,
        destWorkbookId: Long,
        questionIdList: List<Long>
    ) {
        val sourceWorkbook = workBookRepository.getWorkbook(WorkbookId(sourceWorkbookId))
        val destWorkbook = workBookRepository.getWorkbook(WorkbookId(destWorkbookId))
        val questionList =
            sourceWorkbook.questionList.filter { questionIdList.contains(it.id.value) }

        workBookRepository.updateWorkbook(sourceWorkbook.copy(questionList = sourceWorkbook.questionList.filterNot {
            questionIdList.contains(it.id.value)
        }))
        workBookRepository.updateWorkbook(destWorkbook.copy(questionList = destWorkbook.questionList + questionList))
    }

    suspend fun copyQuestionsToOtherWorkbook(
        sourceWorkbookId: Long,
        destWorkbookId: Long,
        questionIdList: List<Long>
    ) {
        val sourceWorkbook = workBookRepository.getWorkbook(WorkbookId(sourceWorkbookId))
        val questionList =
            sourceWorkbook.questionList.filter { questionIdList.contains(it.id.value) }

        questionList.forEach {
            workBookRepository.createQuestion(
                workbookId = WorkbookId(value = destWorkbookId),
                request = CreateQuestionRequest.fromQuestion(it)
            )
        }
    }

    suspend fun copyQuestionInSameWorkbook(
        workbookId: Long,
        questionId: Long
    ) {
        val workbook = workBookRepository.getWorkbook(WorkbookId(workbookId))
        val question = workbook.questionList.find { it.id.value == questionId } ?: return

        workBookRepository.createQuestion(
            workbookId = workbook.id,
            request = CreateQuestionRequest.fromQuestion(question)
        )
    }
}
