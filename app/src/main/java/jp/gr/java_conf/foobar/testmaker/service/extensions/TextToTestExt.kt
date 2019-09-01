package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

fun String.toTest(context: Context, questionId: Long): Test {

    val backups = split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    val test = Test()
    test.title = context.getString(R.string.unknown)
    test.color = context.resources.getIntArray(R.array.color_list)[0]

    for (i in backups.indices) {

        try {

            var backup = backups[i].replace("<br>".toRegex(), "\n").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().filter { s: String -> s != "" }

            backup = backup.map {
                it.replace("<comma>", ",").replace("\r", "")
            }

            if (backup.size > 2) {

                val question = Quest()
                question.problem = backup[1]
                question.id = questionId + test.questionsNonNull().size

                if (backup[0] == context.getString(R.string.load_short_answers)) {

                    question.answer = backup[2]
                    question.type = Constants.WRITE

                } else if (backup[0] == context.getString(R.string.load_multiple_answers)) {
                    if (backup.size - 2 > Constants.ANSWER_MAX) continue

                    question.setAnswers(backup.drop(2).toTypedArray())
                    backup.drop(2).toTypedArray().forEach { question.answer += "$it " }
                    question.type = Constants.COMPLETE

                } else if (backup[0] == context.getString(R.string.load_multiple_answers_order)) {
                    if (backup.size - 2 > Constants.ANSWER_MAX) continue

                    question.setAnswers(backup.drop(2).toTypedArray())
                    question.isCheckOrder = true
                    question.type = Constants.COMPLETE

                } else if (backup[0] == context.getString(R.string.load_selection_problems)) {

                    if (backup.size - 3 > Constants.OTHER_SELECT_MAX) continue

                    question.answer = backup[2]
                    question.setSelections(backup.drop(3).toTypedArray())
                    question.type = Constants.SELECT

                } else if (backup[0] == context.getString(R.string.load_selection_auto_problems)) {

                    if (backup.size != 4) continue

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum > Constants.OTHER_SELECT_MAX) continue

                    question.answer = backup[2]
                    question.auto = true
                    question.type = Constants.SELECT
                    question.setSelections(Array(otherNum) { context.getString(R.string.state_auto) })

                } else if (backup[0] == context.getString(R.string.load_select_complete_auto_problem)) {

                    val otherNum = Integer.parseInt(backup[2].substring(0, 1))

                    if (otherNum > Constants.OTHER_SELECT_MAX) continue

                    val others = Array(otherNum) { context.getString(R.string.state_auto) }

                    val answers = backup.drop(3).toTypedArray()

                    if (others.size + answers.size > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                    question.type = Constants.SELECT_COMPLETE
                    answers.forEach { question.answer += "$it " }
                    question.setAnswers(answers)
                    question.setSelections(others)
                    question.auto = true

                } else if (backup[0] == context.getString(R.string.load_select_complete_problem)) {

                    question.type = Constants.SELECT_COMPLETE

                    val answerNum = Integer.parseInt(backup[2].substring(0, 1))

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum + answerNum > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                    val answers = backup.drop(4).take(answerNum).toTypedArray()

                    val others = backup.drop(4 + answerNum).take(otherNum).toTypedArray()

                    question.type = Constants.SELECT_COMPLETE
                    answers.forEach { question.answer += "$it " }
                    question.setAnswers(answers)
                    question.setSelections(others)

                }

                test.addQuestion(question)

            } else if (backup.size == 2) {

                if (backup[0] == context.getString(R.string.load_explanation)) {
                    if (test.questionsNonNull().isNotEmpty()) {
                        test.questionsNonNull()[test.questionsNonNull().size - 1].explanation = backup[1]
                    }
                } else if (backup[0] == context.getString(R.string.load_title)) {
                    test.title = backup[1]
                } else if (backup[0] == context.getString(R.string.load_category)) {
                    test.setCategory(backup[1])
                } else if (backup[0] == context.getString(R.string.load_color)) {
                    test.color = Integer.parseInt(backup[1])
                }
            }

        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }
    return test

}