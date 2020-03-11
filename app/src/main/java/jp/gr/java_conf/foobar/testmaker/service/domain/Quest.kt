package jp.gr.java_conf.foobar.testmaker.service.domain

import com.google.firebase.auth.FirebaseUser
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.extensions.allIndexed
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseQuestion
import java.util.*

/**
 * Created by keita on 2017/02/08.
 */
open class Quest : RealmObject() {

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

    fun getProblem(isReverse: Boolean = false) = if (isReversible() && isReverse) answer else problem

    fun getAnswer(isReverse: Boolean = false) = if (isReversible() && isReverse) problem else answer

    fun isCorrect(yourAnswer: String, isReverse: Boolean, isCaseInsensitive: Boolean): Boolean =
            if (isCaseInsensitive)
                yourAnswer.toLowerCase(Locale.ENGLISH) == this.getAnswer(isReverse).toLowerCase(Locale.ENGLISH)
            else
                yourAnswer == this.getAnswer(isReverse)

    fun isCorrect(yourAnswers: List<String>, isCaseInsensitive: Boolean): Boolean {

        var isCorrect = if (isCaseInsensitive)
            yourAnswers.all { yourAnswer -> answers.map { it.selection.toLowerCase(Locale.ENGLISH) }.contains(yourAnswer.toLowerCase(Locale.ENGLISH)) }
        else
            yourAnswers.all { yourAnswer -> answers.map { it.selection }.contains(yourAnswer) }

        if (isCorrect) isCorrect = yourAnswers.size == answers.size //必要条件だけ答えてもダメ

        if (isCheckOrder) {
            isCorrect = yourAnswers.allIndexed { index, it ->
                it == answers[index]?.selection
            }
        } else {
            if (isCorrect) isCorrect = yourAnswers.distinct().size == answers.size //同じ解答を繰り返してもダメ
        }

        return isCorrect
    }

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

    fun toFirebaseQuestions(user: FirebaseUser): FirebaseQuestion {

        return FirebaseQuestion(
                question = problem,
                answer = answer,
                answers = answers.map { it.selection },
                others = selections.map { it.selection },
                explanation = explanation,
                imageRef = if (imagePath.isEmpty()) "" else "${user.uid}/$imagePath",
                type = type,
                auto = auto,
                checkOrder = isCheckOrder,
                order = order
        )

    }

    private fun isReversible() = type == Constants.WRITE || type == Constants.COMPLETE

    companion object {
        fun createQuestFromQuestion(question: Question): Quest {
            val quest = Quest()
            quest.id = question.id
            quest.problem = question.question
            quest.answer = question.answer
            quest.explanation = question.explanation
            quest.correct = question.isCorrect
            quest.imagePath = question.imagePath
            quest.setSelections(question.others.toTypedArray())
            quest.setAnswers(question.answers.toTypedArray())
            quest.type = question.type
            quest.auto = question.isAutoGenerateOthers
            quest.solving = question.isSolved
            quest.order = question.order
            quest.isCheckOrder = question.isCheckOrder
            quest.documentId = question.documentId
            return quest
        }
    }

}
