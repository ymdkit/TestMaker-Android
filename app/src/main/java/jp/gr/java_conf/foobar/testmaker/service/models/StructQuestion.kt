package jp.gr.java_conf.foobar.testmaker.service.models

import java.io.Serializable

import jp.gr.java_conf.foobar.testmaker.service.Constants

/**
 * Created by keita on 2016/06/02.
 */
class StructQuestion : Serializable {

    var type: Int = 0
    var answer: String
    var question: String
    var others: Array<String>
    var answers: Array<String>
    var imagePath: String
    var explanation: String
    var light: Boolean = false
    var auto: Boolean = false
    private var order: Int = 0
    var isCheckOrder = false

    constructor(q: String, a: String) {
        question = q
        answer = a
        imagePath = ""
        explanation = ""
        light = false
        others = arrayOf("")
        answers = arrayOf("")
        type = Constants.WRITE
        auto = false
    }

    constructor(q: String, a: Array<String>) {
        question = q
        answer = ""

        for (answer in a) {
            this.answer += "$answer "
        }

        imagePath = ""
        explanation = ""
        light = false
        answers = a
        others = arrayOf("")
        type = Constants.COMPLETE
        auto = false
    }

    constructor(problem: String, answers: Array<String>, others: Array<String>) {
        question = problem
        answer = ""
        for (answer in answers) {
            this.answer += "$answer "
        }
        this.others = others
        this.answers = answers
        type = Constants.SELECT_COMPLETE
        imagePath = ""
        explanation = ""
        light = false
        auto = false
    }

    constructor(q: String, a: String, o: Array<String>) {
        question = q
        answer = a
        others = o
        answers = arrayOf("")
        type = Constants.SELECT
        imagePath = ""
        explanation = ""
        light = false
        auto = false

    }

    fun setOrder(order: Int) {
        this.order = order
    }
}
