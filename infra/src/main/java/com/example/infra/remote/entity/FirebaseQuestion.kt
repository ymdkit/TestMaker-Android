package com.example.infra.remote.entity

import com.example.core.QuestionType
import com.example.domain.model.DocumentId
import com.example.domain.model.SharedQuestion
import com.example.infra.local.entity.RealmQuestion

data class FirebaseQuestion(val question: String = "",
                            val answer: String = "",
                            val answers: List<String> = emptyList(),
                            val others: List<String> = emptyList(),
                            val explanation: String = "",
                            val imageRef: String = "",
                            val type: Int = 0,
                            val auto: Boolean = false,
                            val checkOrder: Boolean = false,
                            val order: Int = 0
) {

    companion object {
        fun fromSharedQuestion(question: SharedQuestion) =
            FirebaseQuestion(
                question = question.problem,
                answer = question.answerList.firstOrNull() ?: "",
                answers = question.answerList,
                others = question.otherSelectionList,
                explanation = question.explanation,
                imageRef = question.problemImageUrl,
                type = question.questionType.value,
                auto = question.isAutoGenerateOtherSelections,
                checkOrder = question.isCheckAnswerOrder,
                order = question.order
            )
    }

    fun toQuest(): RealmQuestion {

        val quest = RealmQuestion()
        quest.problem = question

        if (answer.isNotEmpty()) {
            quest.answer = answer
        } else {
            answers.toTypedArray().forEach { quest.answer += "$it " }
        }
        quest.setAnswers(answers.toTypedArray())
        quest.setSelections(others.toTypedArray())
        quest.auto = auto
        quest.isCheckOrder = checkOrder
        quest.imagePath = imageRef
        quest.explanation = explanation
        quest.type = type

        return quest
    }

    fun toSharedQuestion(documentId: String) = SharedQuestion(
        id = DocumentId(documentId),
        problem = question,
        explanation = explanation,
        answerList = when (type) {
            QuestionType.WRITE.value -> listOf(answer)
            QuestionType.SELECT.value -> listOf(answer)
            QuestionType.COMPLETE.value -> answers
            QuestionType.SELECT_COMPLETE.value -> answers
            else -> listOf(answer)
        },
        otherSelectionList = others,
        problemImageUrl = imageRef,
        explanationImageUrl = "",
        questionType = QuestionType.values().firstOrNull { it.value == type } ?: QuestionType.WRITE,
        isCheckAnswerOrder = checkOrder,
        isAutoGenerateOtherSelections = auto,
        order = order
    )

}
