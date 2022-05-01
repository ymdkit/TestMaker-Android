package com.example.infra.local.entity

import com.example.domain.model.AnswerStatus
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
            when (question) {
                is Question.WriteQuestion -> Quest().apply {
                    id = question.id.value
                    problem = question.problem
                    answer = question.answers.firstOrNull() ?: ""
                    explanation = question.explanation
                    imagePath = question.problemImageUrl
                    correct = question.answerStatus == AnswerStatus.CORRECT
                    solving = question.isAnswering
                    order = question.order
                }
                is Question.SelectQuestion -> Quest().apply {
                    id = question.id.value
                    problem = question.problem
                    answer = question.answers.firstOrNull() ?: ""
                    explanation = question.explanation
                    imagePath = question.problemImageUrl
                    correct = question.answerStatus == AnswerStatus.CORRECT
                    solving = question.isAnswering
                    order = question.order
                    setSelections(question.otherSelections.toTypedArray())
                    auto = question.isAutoGenerateOtherSelections
                }
                is Question.CompleteQuestion -> Quest().apply {
                    id = question.id.value
                    problem = question.problem
                    explanation = question.explanation
                    imagePath = question.problemImageUrl
                    correct = question.answerStatus == AnswerStatus.CORRECT
                    solving = question.isAnswering
                    order = question.order
                    setAnswers(question.answers.toTypedArray())
                    isCheckOrder = question.isCheckAnswerOrder
                }
                is Question.SelectCompleteQuestion -> Quest().apply {
                    id = question.id.value
                    problem = question.problem
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
        when (type) {
            QuestionFormat.WRITE -> Question.WriteQuestion(
                id = QuestionId(id),
                problem = problem,
                answers = listOf(answer),
                explanation = explanation,
                problemImageUrl = imagePath,
                explanationImageUrl = "", // todo カラム追加
                answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
                isAnswering = solving,
                order = order
            )
            QuestionFormat.SELECT -> Question.SelectQuestion(
                id = QuestionId(id),
                problem = problem,
                answers = listOf(answer),
                explanation = explanation,
                problemImageUrl = imagePath,
                explanationImageUrl = "", // todo カラム追加
                answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
                isAnswering = solving,
                order = order,
                otherSelections = selections.map { it.selection },
                isAutoGenerateOtherSelections = auto
            )
            QuestionFormat.COMPLETE -> Question.CompleteQuestion(
                id = QuestionId(id),
                problem = problem,
                explanation = explanation,
                problemImageUrl = imagePath,
                explanationImageUrl = "", // todo カラム追加
                answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
                isAnswering = solving,
                order = order,
                answers = answers.map { it.selection },
                isCheckAnswerOrder = isCheckOrder
            )
            QuestionFormat.SELECT_COMPLETE -> Question.SelectCompleteQuestion(
                id = QuestionId(id),
                problem = problem,
                explanation = explanation,
                problemImageUrl = imagePath,
                explanationImageUrl = "", // todo カラム追加
                answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
                isAnswering = solving,
                order = order,
                otherSelections = selections.map { it.selection },
                isAutoGenerateOtherSelections = auto,
                answers = answers.map { it.selection },
                isCheckAnswerOrder = isCheckOrder
            )
            else -> Question.WriteQuestion(
                id = QuestionId(id),
                problem = "データが破損しています",
                answers = listOf(answer),
                explanation = explanation,
                problemImageUrl = imagePath,
                explanationImageUrl = "", // todo カラム追加
                answerStatus = if (correct) AnswerStatus.CORRECT else AnswerStatus.INCORRECT, // todo 未解答状態への対応
                isAnswering = solving,
                order = order
            )
        }
}

object QuestionFormat {
    const val WRITE = 0
    const val SELECT = 1
    const val COMPLETE = 2
    const val SELECT_COMPLETE = 3
}
