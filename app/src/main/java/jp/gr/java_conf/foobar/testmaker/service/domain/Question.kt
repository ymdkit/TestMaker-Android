package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.extensions.allIndexed
import jp.gr.java_conf.foobar.testmaker.service.infra.api.QuestionResponse
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseQuestion
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
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
                yourAnswer.lowercase(Locale.ENGLISH) == this.getReversibleAnswer(isReverse)
                    .lowercase(Locale.ENGLISH)
            else
                yourAnswer == this.getReversibleAnswer(isReverse)

    fun isCorrect(yourAnswers: List<String>, isCaseInsensitive: Boolean = false): Boolean {
        val yours = if (isCaseInsensitive) yourAnswers.map { it.lowercase(Locale.ENGLISH) } else yourAnswers
        val original = if (isCaseInsensitive) answers.map { it.lowercase(Locale.ENGLISH) } else answers

        if (yours.size != original.size) return false

        if (isCheckOrder) {
            if (!yours.allIndexed { index, it -> it == original[index] }) return false
        } else {
            if (!yours.all { yourAnswer -> original.map { it }.contains(yourAnswer) }) return false
            if (yours.distinct().size != original.size) return false
        }
        return true
    }

    private fun isReversible() = type == Constants.WRITE || type == Constants.COMPLETE

    fun toFirebaseQuestion(imageUrl: String = "") = FirebaseQuestion(
        question = question,
        answer = answer,
        answers = answers,
        others = others,
        explanation = explanation,
        imageRef = imageUrl,
        type = type,
        auto = isAutoGenerateOthers,
        checkOrder = isCheckOrder,
        order = order
    )

    @IgnoredOnParcel
    val hasLocalImage = imagePath.isNotEmpty() && !imagePath.contains("/")

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

        fun createFromQuestionResponse(questionResponse: QuestionResponse, order: Int) = Question(
            question = questionResponse.question,
            answer = questionResponse.answer,
            explanation = questionResponse.explanation,
            answers = questionResponse.answers,
            others = questionResponse.others,
            type = questionResponse.type,
            isCheckOrder = questionResponse.isCheckOrder,
            isAutoGenerateOthers = questionResponse.isAutoGenerateOthers,
            imagePath = questionResponse.imagePath,
            order = order
        )
    }
}