package com.example.usecase

import com.example.usecase.model.QuestionUseCaseModel
import com.example.usecase.model.WorkbookUseCaseModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetOrGenerateSelectionListUseCase @Inject constructor() {

    fun getOrGenerateSelectionList(
        workbook: WorkbookUseCaseModel,
        question: QuestionUseCaseModel,
    ): List<String> =
        if (question.isAutoGenerateOtherSelections) {
            val generatedSelectionList =
                workbook.getRandomExtractedAnswers(exclude = question.answers)
                    .take(question.otherSelections.size)
            (question.answers + generatedSelectionList).shuffled()
        } else {
            (question.answers + question.otherSelections).shuffled()
        }
}
