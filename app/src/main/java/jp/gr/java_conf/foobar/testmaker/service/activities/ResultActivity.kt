package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.ResultAdapter
import jp.studyplus.android.sdk.Studyplus
import jp.studyplus.android.sdk.record.StudyRecord
import jp.studyplus.android.sdk.record.StudyRecordBuilder
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : BaseActivity() {
    private lateinit var resultAdapter: ResultAdapter
    private var testId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        sendScreen("ResultActivity")

        testId = intent.getLongExtra("testId", -1)

        setSupportActionBar(toolbar)

        container.addView(createAd())

        val questions = realmController.getQuestionsSolved(testId)

        resultAdapter = ResultAdapter(this, realmController, testId)

        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = resultAdapter

        var count = 0
        for (i in questions.indices) if (questions[i].correct) count++

        result.text = getString(R.string.message_ratio, count, questions.size)

        top.setOnClickListener { startActivity(Intent(this@ResultActivity, MainActivity::class.java)) }

        retry.setOnClickListener {

            AlertDialog.Builder(this@ResultActivity, R.style.MyAlertDialogStyle)
                    .setTitle(getString(R.string.retry))
                    .setItems(resources.getStringArray(R.array.action_reload)) { _, which ->
                        val i = Intent(this@ResultActivity, PlayActivity::class.java)
                        i.putExtra("testId", testId)

                        if (intent.hasExtra("random")) i.putExtra("random", intent.getIntExtra("random", -1))

                        i.putExtra("redo", 1)

                        when (which) {
                            0 -> {//全問やり直し

                                sharedPreferenceManager.refine = false

                                startActivity(i)
                            }

                            1 -> { //不正解のみやり直し

                                var incorrect = false

                                for (k in questions.indices) if (!questions[k].correct) incorrect = true

                                if (incorrect) {

                                    sharedPreferenceManager.refine = true

                                    startActivity(i)

                                } else {

                                    Toast.makeText(applicationContext, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

                                }
                            }
                        }

                    }.show()
        }


        val record = StudyRecordBuilder()
                .setComment("${realmController.getTest(testId).title} で勉強しました")
                .setAmountTotal(questions.size)
                .setDurationSeconds((intent.getLongExtra("duration",0)/ 1000).toInt())
                .build()


        when(sharedPreferenceManager.uploadStudyPlus){
            Constants.UPLOAD_AUTOMATICALLY_STUDY_PLUS ->
                uploadStudyPlus(record)

            Constants.UPLOAD_MANUALLY_STUDY_PLUS -> {

                upload_study_plus.visibility = View.VISIBLE


            }

        }

        upload_study_plus.setOnClickListener {

            uploadStudyPlus(record)
        }
    }

    private fun uploadStudyPlus(record: StudyRecord){

        if(!Studyplus.instance.isAuthenticated(baseContext)) return

        Studyplus.instance.postRecord(this@ResultActivity, record,
                object : Studyplus.Companion.OnPostRecordListener {
                    override fun onResult(success: Boolean, recordId: Long?, throwable: Throwable?) {
                        if (success) {

                            sendEvent("upload studyplus")

                            Toast.makeText(baseContext, getString(R.string.msg_upload_study_plus), Toast.LENGTH_LONG).show()
                        } else {
                            throwable?.apply {
                                Toast.makeText(baseContext, getString(R.string.msg_failed_upload_study_plus), Toast.LENGTH_LONG).show()
                                printStackTrace()
                            }
                        }
                    }
                })
    }

    override fun onBackPressed() {

        startActivity(Intent(this@ResultActivity, MainActivity::class.java))

        super.onBackPressed()
    }

}
