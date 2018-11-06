package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.os.AsyncTask
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R

open class AsyncTaskLoadTest(private var text: String,private var context: Context) : AsyncTask<String, Int, StructTest>() {

    interface AsyncTaskCallback {
        fun preExecute()
        fun postExecute(result: StructTest)
        fun progressUpdate(progress: Int)
        fun cancel()
    }

    private lateinit var callback: AsyncTaskCallback

    fun setCallback(callback: AsyncTaskCallback) {
        this.callback = callback
    }

    override fun onPreExecute() {
        super.onPreExecute()
        callback.preExecute()
    }

    override fun doInBackground(vararg str: String): StructTest {

        val backups = text.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val q = StructTest(context.getString(R.string.unknown))

        var resultNumber = 0

        for (i in backups.indices) {

            try {

                val backup = backups[i].replace("<br>".toRegex(), "\n").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().filter { s: String -> s != "" }

                if (backup.size > 2) {

                    if (backup[0] == context.getString(R.string.load_short_answers)) {
                        q.setStructQuestion(backup[1], backup[2], resultNumber)
                        resultNumber += 1

                    } else if (backup[0] == context.getString(R.string.load_multiple_answers)) {
                        if (backup.size - 2 <= Constants.ANSWER_MAX) {

                            q.setStructQuestion(backup[1], backup.drop(2).toTypedArray(), resultNumber)
                            resultNumber += 1

                        }

                    } else if (backup[0] == context.getString(R.string.load_selection_problems)) {

                        if (backup.size - 3 <= Constants.OTHER_SELECT_MAX) {

                            q.setStructQuestion(backup[1], backup[2], backup.drop(3).toTypedArray(), resultNumber)
                            resultNumber += 1

                        }

                    } else if (backup[0] == context.getString(R.string.load_selection_auto_problems)) {

                        val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                        if (otherNum <= Constants.OTHER_SELECT_MAX) {

                            val other = Array(otherNum) {context.getString(R.string.state_auto)}

                            q.setStructQuestion(backup[1], backup[2], other, resultNumber)
                            q.problems[resultNumber].auto = true

                            resultNumber += 1

                        }
                    } else if (backup[0] == context.getString(R.string.load_select_complete_auto_problem)) {

                        val otherNum = Integer.parseInt(backup[2].substring(0, 1))

                        if (otherNum <= Constants.OTHER_SELECT_MAX) {

                            val others = Array(otherNum) { context.getString(R.string.state_auto) }

                            val answers = backup.drop(3).toTypedArray()

                            if(others.size + answers.size > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                            q.setStructQuestion(backup[1], answers, others, resultNumber)
                            q.problems[resultNumber].auto = true

                            resultNumber += 1

                        }

                    } else if (backup[0] == context.getString(R.string.load_select_complete_problem)) {

                        val answerNum = Integer.parseInt(backup[2].substring(0, 1))

                        val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                        if(otherNum + answerNum > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                        val answers = backup.drop(4).take(answerNum).toTypedArray()

                        val others = backup.drop(4 + answerNum).take(otherNum).toTypedArray()

                        q.setStructQuestion(backup[1], answers, others, resultNumber)

                        resultNumber += 1
                    }

                } else if (backup.size == 2) {

                    if (backup[0] == context.getString(R.string.load_explanation)) {
                        if (resultNumber > 0) {
                            q.problems[resultNumber - 1].setExplanation(backup[1])
                        }
                    } else if (backup[0] == context.getString(R.string.load_title)) {
                        q.title = backup[1]
                    } else if (backup[0] == context.getString(R.string.load_category)) {
                        q.category = backup[1]
                    } else if (backup[0] == context.getString(R.string.load_color)) {
                        q.color = Integer.parseInt(backup[1])
                    }
                }

            } catch (e: NumberFormatException) {
            } catch (e: ArrayIndexOutOfBoundsException) {
            }
        }
        return q
    }

    override fun onCancelled() {
        super.onCancelled()
        callback.cancel()
    }

    override fun onPostExecute(result: StructTest) {
        callback.postExecute(result)
    }

}