package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.Constants

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter

import java.util.ArrayList

import jp.gr.java_conf.foobar.testmaker.service.R

/**
 * Created by keita on 2017/05/03.
 */

class AsyncLoadTest : AsyncTask<String, Int, StructTest> {

    private val realmController: RealmController
    private var backups: Array<String>? = null
    private var context: Context? = null
    private var errors: ArrayList<Int>? = null

    private var adapter: ScrambleAdapter<Any>? = null

    private var alert: AlertDialog? = null

    private var testId: Long = 0

    constructor(text: Array<String>, recyclerAdapter: ScrambleAdapter<Any>, realm: RealmController, context: Context) {

        adapter = recyclerAdapter
        backups = text
        realmController = realm
        this.context = context
        errors = ArrayList()
        this.testId = -1
    }

    constructor(text: Array<String>, recyclerAdapter: ScrambleAdapter<Any>?, realm: RealmController, context: Context, testId: Long) {

        adapter = recyclerAdapter
        backups = text
        realmController = realm
        this.context = context
        errors = ArrayList()

        this.testId = testId
    }


    // doInBackgroundの事前準備処理（UIスレッド）
    override fun onPreExecute() {
        super.onPreExecute()

        alert = AlertDialog.Builder(context!!, R.style.MyAlertDialogStyle)
                .setTitle(context!!.getString(R.string.loading))
                .setView(R.layout.dialog_progress)
                .show()
    }

    override fun doInBackground(vararg strs: String): StructTest {

        val q = StructTest(context!!.getString(R.string.unknown))

        var resultNumber = 0

        for (i in backups!!.indices) {

            try {

                val backup = backups!![i].replace("<br>".toRegex(), "\n").split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().filter { s: String -> s != "" }

                if (backup.size > 2) {

                    if (backup[0] == context!!.getString(R.string.load_short_answers)) {
                        q.setStructQuestion(backup[1], backup[2], resultNumber)
                        resultNumber += 1

                    } else if (backup[0] == context!!.getString(R.string.load_multiple_answers)) {
                        if (backup.size - 2 <= Constants.ANSWER_MAX) {

                            q.setStructQuestion(backup[1], backup.drop(2).toTypedArray(), resultNumber)
                            resultNumber += 1

                        }

                    } else if (backup[0] == context!!.getString(R.string.load_selection_problems)) {

                        if (backup.size - 3 <= Constants.OTHER_SELECT_MAX) {

                            q.setStructQuestion(backup[1], backup[2], backup.drop(3).toTypedArray(), resultNumber)
                            resultNumber += 1

                        }

                    } else if (backup[0] == context!!.getString(R.string.load_selection_auto_problems)) {

                        val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                        if (otherNum <= Constants.OTHER_SELECT_MAX) {

                            val other = Array(otherNum) {context!!.getString(R.string.state_auto)}

                            q.setStructQuestion(backup[1], backup[2], other, resultNumber)
                            q.problems[resultNumber].auto = true

                            resultNumber += 1

                        }
                    } else if (backup[0] == context!!.getString(R.string.load_select_complete_auto_problem)) {

                        val otherNum = Integer.parseInt(backup[2].substring(0, 1))

                        if (otherNum <= Constants.OTHER_SELECT_MAX) {

                            val others = Array(otherNum) { context!!.getString(R.string.state_auto) }

                            val answers = backup.drop(3).toTypedArray()

                            if(others.size + answers.size > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                            q.setStructQuestion(backup[1], answers, others, resultNumber)
                            q.problems[resultNumber].auto = true

                            resultNumber += 1

                        }

                    } else if (backup[0] == context!!.getString(R.string.load_select_complete_problem)) {

                        val answerNum = Integer.parseInt(backup[2].substring(0, 1))

                        val otherNum = Integer.parseInt(backup[3].substring(0, 1))

                        if(otherNum + answerNum > Constants.SELECT_COMPLETE_MAX) continue //要素数オーバー

                        val answers = backup.drop(4).take(answerNum).toTypedArray()

                        val others = backup.drop(4 + answerNum).take(otherNum).toTypedArray()

                        q.setStructQuestion(backup[1], answers, others, resultNumber)

                        resultNumber += 1
                    }

                } else if (backup.size == 2) {

                    if (backup[0] == context!!.getString(R.string.load_explanation)) {
                        if (resultNumber > 0) {
                            q.problems[resultNumber - 1].setExplanation(backup[1])
                        }
                    } else if (backup[0] == context!!.getString(R.string.load_title)) {
                        q.title = backup[1]
                    } else if (backup[0] == context!!.getString(R.string.load_category)) {
                        q.category = backup[1]
                    } else if (backup[0] == context!!.getString(R.string.load_color)) {
                        q.color = Integer.parseInt(backup[1])
                    }
                }

            } catch (e: NumberFormatException) {
                errors!!.add(i + 1)
            } catch (e: ArrayIndexOutOfBoundsException) {
                errors!!.add(i + 1)
            }

        }

        return q
    }

    // doInBackgroundの事後処理(UIスレッド)
    override fun onPostExecute(result: StructTest) {

        val error = StringBuilder()

        for (i in errors!!.indices) {
            error.append(errors!![i].toString()).append(" ")
        }

        if (error.toString() != "") {
            Toast.makeText(context, context!!.getString(R.string.message_wrong_load, error.toString()), Toast.LENGTH_LONG).show()
        } else {

            if (testId != -1L) {
                Toast.makeText(context, context!!.getString(R.string.message_success_update), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, context!!.getString(R.string.message_success_load, result.title), Toast.LENGTH_LONG).show()
            }
        }


        realmController.convert(result, testId)

        if (adapter != null) {
            adapter!!.notifyDataSetChanged()
        }

        alert!!.dismiss()

    }
}
