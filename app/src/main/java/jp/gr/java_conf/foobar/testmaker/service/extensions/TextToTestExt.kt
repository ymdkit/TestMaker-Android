package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun String.toTest(context: Context): Test = withContext(Dispatchers.Default) {

    val backups = split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

    var test = Test(title = context.getString(R.string.unknown), color = context.resources.getIntArray(R.array.color_list)[0])

    for (i in backups.indices) {
        try {
            var backup = backups[i].replace("<br>".toRegex(), "\n").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().filter { s: String -> s != "" }

            backup = backup.map {
                it.replace("<comma>", ",").replace("\r", "")
            }

            if (backup.size > 2) {

                var question = Question(question = backup[1])

                if (backup[0] == context.getString(R.string.load_short_answers)) {

                    question = question.copy(
                            answer = backup[2],
                            type = Constants.WRITE
                    )

                } else if (backup[0] == context.getString(R.string.load_multiple_answers)) {
                    if (backup.size - 2 > Constants.ANSWER_MAX) continue

                    question = question.copy(
                            answers = backup.drop(2),
                            answer = backup.drop(2).joinToString(separator = " "),
                            type = Constants.COMPLETE
                    )

                } else if (backup[0] == context.getString(R.string.load_multiple_answers_order)) {
                    if (backup.size - 2 > Constants.ANSWER_MAX) continue

                    question = question.copy(
                            answers = backup.drop(2),
                            answer = backup.drop(2).joinToString(separator = " "),
                            type = Constants.COMPLETE,
                            isCheckOrder = true
                    )

                } else if (backup[0] == context.getString(R.string.load_selection_problems)) {

                    if (backup.size - 3 > Constants.OTHER_SELECT_MAX) continue

                    question = question.copy(
                            answer = backup[2],
                            others = backup.drop(3),
                            type = Constants.SELECT
                    )

                } else if (backup[0] == context.getString(R.string.load_selection_auto_problems)) {

                    if (backup.size != 4) continue

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum > Constants.OTHER_SELECT_MAX) continue

                    question = question.copy(
                            answer = backup[2],
                            others = List(otherNum) { context.getString(R.string.state_auto) },
                            type = Constants.SELECT
                    )

                } else if (backup[0] == context.getString(R.string.load_select_complete_auto_problem)) {

                    val otherNum = Integer.parseInt(backup[2].substring(0, 1))

                    if (otherNum > Constants.OTHER_SELECT_MAX) continue

                    val others = List(otherNum) { context.getString(R.string.state_auto) }

                    val answers = backup.drop(3)

                    if (others.size + answers.size > Constants.SELECT_COMPLETE_MAX) continue // 要素数オーバー

                    question = question.copy(
                            answer = backup.drop(2).joinToString(separator = " "),
                            answers = answers,
                            others = others,
                            type = Constants.SELECT_COMPLETE,
                            isAutoGenerateOthers = true
                    )

                } else if (backup[0] == context.getString(R.string.load_select_complete_problem)) {

                    question.type = Constants.SELECT_COMPLETE

                    val answerNum = Integer.parseInt(backup[2].substring(0, 1))

                    val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                    if (otherNum + answerNum > Constants.SELECT_COMPLETE_MAX) continue // 要素数オーバー

                    val answers = backup.drop(4).take(answerNum)

                    val others = backup.drop(4 + answerNum).take(otherNum)

                    question = question.copy(
                            answer = backup.drop(2).joinToString(separator = " "),
                            answers = answers,
                            others = others,
                            type = Constants.SELECT_COMPLETE
                    )
                }

                test = test.copy(
                        questions = test.questions + listOf(question)
                )

            } else if (backup.size == 2) {

                if (backup[0] == context.getString(R.string.load_explanation)) {
                    if (test.questions.isNotEmpty()) {
                        test.questions[test.questions.size - 1].explanation = backup[1]
                    }
                } else if (backup[0] == context.getString(R.string.load_title)) {
                    test = test.copy(
                            title = backup[1]
                    )
                } else if (backup[0] == context.getString(R.string.load_category)) {
                    test = test.copy(
                            category = backup[1]
                    )
                } else if (backup[0] == context.getString(R.string.load_color)) {
                    test = test.copy(
                            color = Integer.parseInt(backup[1])
                    )
                }
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
        }
    }
    test
}
