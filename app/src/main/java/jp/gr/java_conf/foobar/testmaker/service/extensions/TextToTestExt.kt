package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.StructTest
import jp.gr.java_conf.foobar.testmaker.service.models.Test

fun String.toTest(context: Context): Test {

    val backups = split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    val test = StructTest(context.getString(R.string.unknown))

    val t = Test()

    var resultNumber = 0

    for (i in backups.indices) {

        try {

            var backup = backups[i].replace("<br>".toRegex(), "\n").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().filter { s: String -> s != "" }

            backup = backup.map {
                it.replace("<comma>", ",").replace("\r", "")
            }

            if (backup.size > 2) {

                if (backup[0] == context.getString(R.string.load_short_answers)) {

                    val q = Quest()
                    q.problem = backup[1]
                    q.answer = backup[2]
                    t.addQuestion(q)

                    //test.setStructQuestion(backup[1], backup[2], resultNumber)
                    resultNumber += 1

                } else if (backup[0] == context.getString(R.string.load_multiple_answers)) {
                    if (backup.size - 2 <= Constants.ANSWER_MAX) {



                        test.setStructQuestion(backup[1], backup.drop(2).toTypedArray(), resultNumber)
                        resultNumber += 1

                    }

                } else if (backup[0] == context.getString(R.string.load_multiple_answers_order)) {
                    if (backup.size - 2 <= Constants.ANSWER_MAX) {

                        test.setStructQuestion(backup[1], backup.drop(2).toTypedArray(), resultNumber)
                        test.problems[resultNumber].isCheckOrder = true
                        resultNumber += 1

                    }

                } else if (backup[0] == context.getString(R.string.load_selection_problems)) {

                    if (backup.size - 3 <= Constants.OTHER_SELECT_MAX) {

                        test.setStructQuestion(backup[1], backup[2], backup.drop(3).toTypedArray(), resultNumber)
                        resultNumber += 1

                    }

                } else if (backup[0] == context.getString(R.string.load_selection_auto_problems)) {

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum <= Constants.OTHER_SELECT_MAX) {

                        val other = Array(otherNum) { context.getString(R.string.state_auto) }

                        test.setStructQuestion(backup[1], backup[2], other, resultNumber)
                        test.problems[resultNumber].auto = true

                        resultNumber += 1

                    }
                } else if (backup[0] == context.getString(R.string.load_select_complete_auto_problem)) {

                    val otherNum = Integer.parseInt(backup[2].substring(0, 1))

                    if (otherNum <= Constants.OTHER_SELECT_MAX) {

                        val others = Array(otherNum) { context.getString(R.string.state_auto) }

                        val answers = backup.drop(3).toTypedArray()

                        if (others.size + answers.size > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                        test.setStructQuestion(backup[1], answers, others, resultNumber)
                        test.problems[resultNumber].auto = true

                        resultNumber += 1

                    }

                } else if (backup[0] == context.getString(R.string.load_select_complete_problem)) {

                    val answerNum = Integer.parseInt(backup[2].substring(0, 1))

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum + answerNum > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                    val answers = backup.drop(4).take(answerNum).toTypedArray()

                    val others = backup.drop(4 + answerNum).take(otherNum).toTypedArray()

                    test.setStructQuestion(backup[1], answers, others, resultNumber)

                    resultNumber += 1
                }

            } else if (backup.size == 2) {

                if (backup[0] == context.getString(R.string.load_explanation)) {
                    if (resultNumber > 0) {
                        test.problems[resultNumber - 1].explanation = (backup[1])
                    }
                } else if (backup[0] == context.getString(R.string.load_title)) {
                    t.title = backup[1]
                    test.title = backup[1]
                } else if (backup[0] == context.getString(R.string.load_category)) {
                    test.category = backup[1]
                } else if (backup[0] == context.getString(R.string.load_color)) {
                    test.color = Integer.parseInt(backup[1])
                }
            }

        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }
    return t

}