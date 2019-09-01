package jp.gr.java_conf.foobar.testmaker.service.domain

import com.google.firebase.auth.FirebaseUser
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseQuestion

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

    fun getProblem(isReverse: Boolean): String {

        return if (isReverse) answer else problem

    }

    fun getAnswer(isReverse: Boolean): String {

        return if (isReverse) problem else answer

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
                isAuto = auto,
                isCheckOrder = isCheckOrder,
                order = order
        )

    }

}
