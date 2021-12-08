package jp.gr.java_conf.foobar.testmaker.service.domain

import jp.gr.java_conf.foobar.testmaker.service.extensions.allIndexed

data class QuestionModel(
    val id: Long,
    val problem: String,
    val answer: String,
    val answers: List<String>,
    val wrongChoices: List<String>,
    val format: QuestionFormat,
    val imageUrl: String,
    val explanation: String,
    val isAutoGenerateWrongChoices: Boolean,
    val isCheckOrder: Boolean,
    val isAnswering: Boolean,
    val answerStatus: AnswerStatus,
){
    fun isCorrect(yourAnswer: String): Boolean =
        yourAnswer == answer

    fun isCorrect(yourAnswers: List<String>): Boolean {
        val original = answers

        if (yourAnswers.size != original.size) return false

        if (isCheckOrder) {
            if (!yourAnswers.allIndexed { index, it -> it == original[index] }) return false
        } else {
            if (!yourAnswers.all { yourAnswer ->
                    original.map { it }.contains(yourAnswer)
                }) return false
            if (yourAnswers.distinct().size != original.size) return false
        }
        return true
    }

    fun getAnswerForReview() =
        when (format) {
            QuestionFormat.WRITE, QuestionFormat.SELECT -> answer
            QuestionFormat.COMPLETE, QuestionFormat.SELECT_COMPLETE -> answers.joinToString("\n")
        }

    fun getAnswerForResult() =
        when (format) {
            QuestionFormat.WRITE, QuestionFormat.SELECT -> answer
            QuestionFormat.COMPLETE, QuestionFormat.SELECT_COMPLETE -> answers.joinToString(" ")
        }

    fun toQuestion() = Question(
        id = id,
        question = problem,
        answer = answer,
        answers = answers,
        others = wrongChoices,
        imagePath = imageUrl,
        explanation = explanation,
        type = format.getTypeId(),
        isAutoGenerateOthers = isAutoGenerateWrongChoices,
        isCheckOrder = isCheckOrder,
        isSolved = isAnswering,
        isCorrect = answerStatus == AnswerStatus.CORRECT
    )
}

enum class QuestionFormat(val rawValue: String) {
    WRITE("write"),
    SELECT("select"),
    COMPLETE("complete"),
    SELECT_COMPLETE("select_complete");

    fun getTypeId() = when(this) {
        WRITE -> 0
        SELECT -> 1
        COMPLETE -> 2
        SELECT_COMPLETE -> 3
    }
}

enum class AnswerStatus(val rawValue: String) {
    UNANSWERED("unanswered"),
    INCORRECT("incorrect"),
    CORRECT("correct")
}
