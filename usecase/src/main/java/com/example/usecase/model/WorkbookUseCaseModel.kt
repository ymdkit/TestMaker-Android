package com.example.usecase.model

import com.example.domain.model.AnswerStatus
import com.example.domain.model.Workbook

data class WorkbookUseCaseModel(
    val id: Long,
    val name: String,
    val color: Int, // todo don't use resId directly
    val folderName: String,
    val questionCount: Int,
    val correctCount: Int,
    val inCorrectCount: Int
) {
    companion object {
        fun fromWorkbook(workbook: Workbook): WorkbookUseCaseModel =
            WorkbookUseCaseModel(
                id = workbook.id.value,
                name = workbook.name,
                color = workbook.color,
                folderName = workbook.folderName,
                questionCount = workbook.questionList.count(),
                correctCount = workbook.questionList.count { it.answerStatus == AnswerStatus.CORRECT },
                inCorrectCount = workbook.questionList.count { it.answerStatus == AnswerStatus.INCORRECT },
            )
    }
}
