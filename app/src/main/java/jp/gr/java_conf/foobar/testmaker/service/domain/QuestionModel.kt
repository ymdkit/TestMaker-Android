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
    val order: Int
) {
    fun isCorrect(yourAnswer: String): Boolean =
        yourAnswer == answer

    fun isCorrect(yourAnswers: List<String>, isSwap: Boolean): Boolean {
        val original = getAnswers(isSwap)

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

    fun getProblem(isSwap: Boolean) =
        when (format) {
            QuestionFormat.WRITE -> if (isSwap) answer else problem
            QuestionFormat.SELECT -> problem
            QuestionFormat.COMPLETE -> if (isSwap) answers.joinToString(separator = "\n") else problem
            QuestionFormat.SELECT_COMPLETE -> problem
        }

    fun getAnswer(isSwap: Boolean) =
        when (format) {
            QuestionFormat.WRITE -> if (isSwap) problem else answer
            QuestionFormat.SELECT -> answer
            QuestionFormat.COMPLETE -> if (isSwap) problem else ""
            QuestionFormat.SELECT_COMPLETE -> ""
        }

    fun getAnswers(isSwap: Boolean) = when (format) {
        QuestionFormat.WRITE -> emptyList()
        QuestionFormat.SELECT -> emptyList()
        QuestionFormat.COMPLETE -> if (isSwap) listOf(problem) else answers
        QuestionFormat.SELECT_COMPLETE -> answers
    }

    fun getAnswerForReview(isSwap: Boolean) =
        when (format) {
            QuestionFormat.WRITE -> if (isSwap) problem else answer
            QuestionFormat.SELECT -> answer
            QuestionFormat.COMPLETE -> if (isSwap) problem else answers.joinToString("\n")
            QuestionFormat.SELECT_COMPLETE -> answers.joinToString("\n")
        }

    fun getAnswerForResult() =
        when (format) {
            QuestionFormat.WRITE, QuestionFormat.SELECT -> answer
            QuestionFormat.COMPLETE, QuestionFormat.SELECT_COMPLETE -> answers.joinToString(" ")
        }

    fun getChoices(candidates: List<String>) =
        when (format) {
            QuestionFormat.WRITE -> emptyList()
            QuestionFormat.SELECT ->
                if (isAutoGenerateWrongChoices) {
                    (listOf(answer) + candidates.take(wrongChoices.size)).shuffled()
                } else {
                    (listOf(answer) + wrongChoices).shuffled()
                }
            QuestionFormat.COMPLETE -> emptyList()
            QuestionFormat.SELECT_COMPLETE ->
                if (isAutoGenerateWrongChoices) {
                    (answers + candidates.take(wrongChoices.size)).shuffled()
                } else {
                    (answers + wrongChoices).shuffled()
                }
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
        isCorrect = answerStatus == AnswerStatus.CORRECT,
        order = order
    )
}

enum class QuestionFormat(val rawValue: String) {
    WRITE("write"),
    SELECT("select"),
    COMPLETE("complete"),
    SELECT_COMPLETE("select_complete");

    fun getTypeId() = when (this) {
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
