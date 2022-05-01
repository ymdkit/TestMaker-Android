package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import com.example.infra.local.entity.Quest
import com.example.infra.remote.ImportQuestionResponse
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseQuestion
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

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

    @IgnoredOnParcel
    val singleLineAnswer =
        when(type){
            Constants.WRITE, Constants.SELECT -> answer
            Constants.COMPLETE, Constants.SELECT_COMPLETE -> answers.joinToString(" ")
            else -> answer
        }

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


    fun toQuestionModel() = QuestionModel(
        id = id,
        problem = question,
        answer = answer,
        answers = answers,
        wrongChoices = others,
        format = format,
        imageUrl = imagePath,
        explanation = explanation,
        isAutoGenerateWrongChoices = isAutoGenerateOthers,
        isCheckOrder = isCheckOrder,
        isAnswering = isSolved,
        answerStatus = if (isCorrect) AnswerStatus.CORRECT else AnswerStatus.INCORRECT,
        order = order
    )

    fun toRealmQuestion(): Quest {
        val quest = Quest()
        quest.id = id
        quest.problem = question
        quest.answer = answer
        quest.explanation = explanation
        quest.correct = isCorrect
        quest.imagePath = imagePath
        quest.setSelections(others.toTypedArray())
        quest.setAnswers(answers.toTypedArray())
        quest.type = type
        quest.auto = isAutoGenerateOthers
        quest.solving = isSolved
        quest.order = order
        quest.isCheckOrder = isCheckOrder
        quest.documentId = documentId
        return quest
    }

    @IgnoredOnParcel
    private val format = when (type) {
        Constants.WRITE -> QuestionFormat.WRITE
        Constants.SELECT -> QuestionFormat.SELECT
        Constants.COMPLETE -> QuestionFormat.COMPLETE
        Constants.SELECT_COMPLETE -> QuestionFormat.SELECT_COMPLETE
        else -> QuestionFormat.WRITE
    }

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

        fun createFromQuestionResponse(questionResponse: ImportQuestionResponse, order: Int) =
            Question(
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