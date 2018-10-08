package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.util.Log
import io.realm.Realm

import java.util.Calendar

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.log.RealmLog
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.Constants

/**
 * Created by keita on 2017/02/08.
 */

open class Test : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var color: Int = 0
    var limit: Int = 0
    var title: String? = null
    private var category: String? = null

    var history: Long = 0

    private var questions: RealmList<Quest>? = null

    val questionsCorrectCount: Int
        get() {

            if (questions == null) return 0

            var count = 0

            for (question in questions!!) {

                if (question.correct) {

                    count++

                }

            }

            return count
        }

    fun setHistory() {
        val c = Calendar.getInstance()
        history = c.timeInMillis
    }

    fun setCategory(c: String) {
        category = c
    }

    fun getCategory(): String {
        
        return category?: ""

    }

    fun addQuestion(q:Quest){
        questions?.add(q)
    }

    fun getQuestions(): RealmList<Quest> {

        val questions = this.questions?: RealmList()

        val results = RealmList<Quest>()
        results.addAll(questions.sort("order").subList(0, questions.size))

        return results
        
    }

    fun getQuestionsForEach(): RealmList<Quest>{//順番はどうでもいいが全てにアクセスしたい時

        return questions?: RealmList()

    }

    fun setQuestions(q: RealmList<Quest>) {
        questions = q
    }

    fun testToString(context: Context,upload: Boolean): String {

        var backup = ""

        val questions = getQuestions()

        for (i in questions.indices) {
            val q = questions[i] ?: Quest()

            if(q.imagePath != "" && upload) continue //画像付き問題はスキップ

            var lineWrite = StringBuilder()

            when (q.type) {
                Constants.WRITE -> lineWrite = StringBuilder(context.getString(R.string.share_short_answers, q.problem, q.answer))

                Constants.COMPLETE -> {

                    lineWrite = StringBuilder(context.getString(R.string.share_multiple_answers, q.problem))

                    for (k in 0 until q.answers.size) {
                        lineWrite.append(q.answers[k]!!.selection).append(",")
                    }
                    lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))
                }
                Constants.SELECT -> if (q.auto) {
                    lineWrite = StringBuilder(context.getString(R.string.share_selection_auto_problems, q.problem, q.answer, q.selections.size))

                } else {
                    lineWrite = StringBuilder(context.getString(R.string.share_selection_problems, q.problem, q.answer))

                    for (k in 0 until q.selections.size) {
                        lineWrite.append(q.selections[k]!!.selection).append(",")
                    }
                    lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                }

                Constants.SELECT_COMPLETE ->

                    if (q.auto) {
                        lineWrite = StringBuilder(context.getString(R.string.share_select_complete_auto_problem, q.problem))

                        lineWrite.append(q.selections.size.toString()).append(",")

                        for (k in 0 until q.answers.size) {
                            lineWrite.append(q.answers[k]!!.selection).append(",")
                        }

                        lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                    } else {
                        lineWrite = StringBuilder(context.getString(R.string.share_select_complete_problem, q.problem))

                        lineWrite.append(q.answers.size.toString()).append(",")

                        lineWrite.append(q.selections.size.toString()).append(",")

                        for (k in 0 until q.answers.size) {
                            lineWrite.append(q.answers[k]!!.selection).append(",")
                        }

                        for (k in 0 until q.selections.size) {
                            lineWrite.append(q.selections[k]!!.selection).append(",")
                        }
                        lineWrite = StringBuilder(lineWrite.substring(0, lineWrite.length - 1))

                    }
            }

            if (lineWrite.toString().contains("\n")) {
                lineWrite = StringBuilder(lineWrite.toString().replace("\n".toRegex(), "<br>"))
            }

            backup += lineWrite

            if (q.explanation != "") {
                backup += "\n"
                backup += context.getString(R.string.share_explanation, q.explanation)
            }

            backup += "\n"

        }

        backup += context.getString(R.string.share_title, title)
        backup += "\n"
        backup += context.getString(R.string.share_category, getCategory())
        backup += "\n"
        backup += context.getString(R.string.share_color, color.toString())

        return backup

    }
}
