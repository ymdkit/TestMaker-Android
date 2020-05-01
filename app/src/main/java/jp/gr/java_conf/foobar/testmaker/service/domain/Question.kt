package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.extensions.allIndexed
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class Question(
        val id: Long = 0,
        val question: String = "",
        val answer: String = "",
        var explanation: String = "",
        var isCorrect: Boolean = false,
        var imagePath: String = "",
        var others: List<String> = emptyList(),
        var answers: List<String> = emptyList(),
        var type: Int = 0,
        var isAutoGenerateOthers: Boolean = false,
        var isSolved: Boolean = false,
        var order: Int = 0,
        var isCheckOrder: Boolean = false,
        var documentId: String = ""
) : Parcelable {

    fun getReversibleAnswer(isReverse: Boolean = false) = if (isReversible() && isReverse) question else answer

    fun getAnswerForReview() =
            when (type) {
                Constants.WRITE, Constants.SELECT -> answer
                Constants.COMPLETE, Constants.SELECT_COMPLETE -> answers.joinToString("\n")
                else -> answer
            }


    fun isCorrect(yourAnswer: String, isReverse: Boolean, isCaseInsensitive: Boolean): Boolean =
            if (isCaseInsensitive)
                yourAnswer.toLowerCase(Locale.ENGLISH) == this.getReversibleAnswer(isReverse).toLowerCase(Locale.ENGLISH)
            else
                yourAnswer == this.getReversibleAnswer(isReverse)

    fun isCorrect(yourAnswers: List<String>, isCaseInsensitive: Boolean): Boolean {

        var isCorrect = if (isCaseInsensitive)
            yourAnswers.all { yourAnswer -> answers.map { it.toLowerCase(Locale.ENGLISH) }.contains(yourAnswer.toLowerCase(Locale.ENGLISH)) }
        else
            yourAnswers.all { yourAnswer -> answers.map { it }.contains(yourAnswer) }

        if (isCorrect) isCorrect = yourAnswers.size == answers.size //必要条件だけ答えてもダメ

        if (isCheckOrder) {
            isCorrect = yourAnswers.allIndexed { index, it ->
                it == answers[index]
            }
        } else {
            if (isCorrect) isCorrect = yourAnswers.distinct().size == answers.size //同じ解答を繰り返してもダメ
        }

        return isCorrect
    }

    fun getFirebaseEvent() =
            when (type) {
                Constants.WRITE -> "play_write_question"
                Constants.SELECT -> "play_select_question" + if (isAutoGenerateOthers) "_auto" else ""
                Constants.COMPLETE -> "play_complete_question" + if (isCheckOrder) "_ordered" else ""
                Constants.SELECT_COMPLETE -> "play_select_complete_question" + if (isCheckOrder) "_ordered" else "" + if (isAutoGenerateOthers) "_auto" else ""
                else -> ""
            } + if (imagePath.isNotEmpty()) "_image" else "" + if (explanation.isNotEmpty()) "_explanation" else ""

    private fun isReversible() = type == Constants.WRITE || type == Constants.COMPLETE

    companion object {
        fun createFromRealmQuestion(realmQuestion: Quest) = Question(
                id = realmQuestion.id,
                question = realmQuestion.problem,
                answer = realmQuestion.answer,
                explanation = realmQuestion.explanation,
                isCorrect = realmQuestion.correct,
                imagePath = realmQuestion.imagePath,
                others = realmQuestion.selections.map { it.selection },
                answers = realmQuestion.answers.map { it.selection },
                type = realmQuestion.type,
                isAutoGenerateOthers = realmQuestion.auto,
                isSolved = realmQuestion.solving,
                order = realmQuestion.order,
                isCheckOrder = realmQuestion.isCheckOrder,
                documentId = realmQuestion.documentId

        )
    }
}