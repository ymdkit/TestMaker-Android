package com.example.usecase.model

import com.example.core.AnswerStatus
import com.example.domain.model.Workbook

data class WorkbookUseCaseModel(
    val id: Long,
    val remoteId: String,
    val name: String,
    val color: Int, // todo don't use resId directly
    val folderName: String,
    val questionList: List<QuestionUseCaseModel>,
    val questionCount: Int,
    val correctCount: Int,
    val inCorrectCount: Int
) {
    companion object {
        fun fromWorkbook(workbook: Workbook): WorkbookUseCaseModel =
            WorkbookUseCaseModel(
                id = workbook.id.value,
                remoteId = workbook.remoteId,
                name = workbook.name,
                color = workbook.color,
                folderName = workbook.folderName,
                questionList = workbook.questionList.map { QuestionUseCaseModel.fromQuestion(it) }
                    .sortedBy { it.order },
                questionCount = workbook.questionList.count(),
                correctCount = workbook.questionList.count { it.answerStatus == AnswerStatus.CORRECT },
                inCorrectCount = workbook.questionList.count { it.answerStatus == AnswerStatus.INCORRECT },
            )
    }

    val isQuestionListEmpty = questionCount == 0

    fun getRandomExtractedAnswers(
        exclude: List<String>
    ) =
        questionList
            .asSequence()
            .take(100)
            .map {
                it.answers
            }
            .flatten()
            .filter { !exclude.contains(it) }
            .distinct()
            .toList()
            .shuffled()
}
