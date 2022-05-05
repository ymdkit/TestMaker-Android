package com.example.infra.local.entity

import com.example.core.AnswerStatus
import com.example.core.QuestionType
import com.example.domain.model.CreateQuestionRequest
import com.example.domain.model.Question
import com.example.domain.model.QuestionId
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required

/**
 * Created by keita on 2017/02/08.
 */
open class Quest : RealmObject() {

    companion object {
        fun fromQuestion(question: Question) =
            Quest().apply {
                id = question.id.value
                type = question.type.value
                problem = question.problem
                answer = question.answers.firstOrNull() ?: ""
                explanation = question.explanation
                imagePath = question.problemImageUrl
                correct = question.answerStatus == AnswerStatus.CORRECT
                solving = question.isAnswering
                order = question.order
                setSelections(question.otherSelections.toTypedArray())
                setAnswers(question.answers.toTypedArray())
                isCheckOrder = question.isCheckAnswerOrder
                auto = question.isAutoGenerateOtherSelections
            }

        fun fromCreateQuestionRequest(questionId: Long, request: CreateQuestionRequest) =
            Quest().apply {
                this.id = questionId
                this.problem = request.problem
                this.answer = request.answers.firstOrNull() ?: ""
                this.type = request.questionType.value
                this.explanation = request.explanation
                this.imagePath = request.problemImageUrl
                this.order = questionId.toInt()
                this.setSelections(request.otherSelections.toTypedArray())
                this.setAnswers(request.answers.toTypedArray())
                this.isCheckOrder = request.isCheckAnswerOrder
                this.auto = request.isAutoGenerateOtherSelections
            }
    }

    @PrimaryKey
    var id: Long = 0
    var problem: String = ""
    var answer: String = ""

    @Required
    var explanation: String = ""
    var correct: Boolean = false
    var imagePath: String = ""
    var selections: RealmList<Select> = RealmList()
    var answers: RealmList<Select> = RealmList()
    var type: Int = 0
    var auto: Boolean = false
    var solving: Boolean = false
    var order: Int = 0
    var isCheckOrder: Boolean = false
    var documentId: String = ""

    fun setSelections(strings: Array<String>) {

        selections.clear()

        for (string in strings) {
            val select = Select()
            select.selection = string
            selections.add(select)
        }

    }

    fun setAnswers(strings: Array<String>) {

        answers.clear()

        for (string in strings) {
            val select = Select()
            select.selection = string
            answers.add(select)
        }

    }

    fun toQuestion(): Question =
        Question(
            id = QuestionId(id),
            type = QuestionType.valueOf(type),
            problem = problem,
            explanation = explanation,
            problemImageUrl = imagePath,
            explanationImageUrl = "", // todo カラム追加
            answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
            isAnswering = solving,
            order = order,
            otherSelections = selections.map { it.selection },
            isAutoGenerateOtherSelections = auto,
            answers = when (QuestionType.valueOf(type)) {
                QuestionType.WRITE, QuestionType.SELECT -> listOf(answer)
                QuestionType.COMPLETE, QuestionType.SELECT_COMPLETE -> answers.map { it.selection }
            },
            isCheckAnswerOrder = isCheckOrder
        )
}
