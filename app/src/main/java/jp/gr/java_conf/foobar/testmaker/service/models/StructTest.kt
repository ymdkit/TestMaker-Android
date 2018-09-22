package jp.gr.java_conf.foobar.testmaker.service.models

import java.io.Serializable
import java.util.ArrayList

import jp.gr.java_conf.foobar.testmaker.service.R

/**
 * Created by keita on 2016/05/22.
 */
class StructTest internal constructor(var title: String?) : Serializable {
    var color: Int = 0
    var category: String? = null
    val history: Long

    var problems: ArrayList<StructQuestion>

    init {
        color = R.color.white
        problems = ArrayList()
        category = ""
        history = 0
    }

    fun setStructQuestion(question: String, answer: String, position: Int) {
        if (problems.size <= position) {
            problems.add(StructQuestion(question, answer))
        } else {
            problems[position] = StructQuestion(question, answer)
        }
    }

    fun setStructQuestion(question: String, answers: Array<String>, position: Int) {
        if (problems.size <= position) {
            problems.add(StructQuestion(question, answers))
        } else {
            problems[position] = StructQuestion(question, answers)
        }
    }


    fun setStructQuestion(question: String, answer: String, others: Array<String>, position: Int) {
        if (problems.size <= position) {
            problems.add(StructQuestion(question, answer, others))
        } else {
            problems[position] = StructQuestion(question, answer, others)
        }
    }

    fun setStructQuestion(question: String, answers: Array<String>, others: Array<String>, position: Int) {
        if (problems.size <= position) {
            problems.add(StructQuestion(question, answers, others))
        } else {
            problems[position] = StructQuestion(question, answers, others)
        }
    }

}
