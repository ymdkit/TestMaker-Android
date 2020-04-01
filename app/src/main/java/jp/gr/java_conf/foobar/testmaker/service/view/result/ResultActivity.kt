package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityNewResultBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditQuestionActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.SelfJudgePlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.studyplus.android.sdk.Studyplus
import jp.studyplus.android.sdk.record.StudyRecord
import jp.studyplus.android.sdk.record.StudyRecordAmountTotal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ResultActivity : BaseActivity() {

    private lateinit var test: Test
    private val testViewModel: TestViewModel by viewModel()

    private val questions by lazy { test.questions.filter { it.isSolved } }

    private val binding by lazy { DataBindingUtil.setContentView<ActivityNewResultBinding>(this, R.layout.activity_new_result) }

    private val controller by lazy {
        ResultController(this, object : ResultController.OnClickQuestionListener {
            override fun onClickQuestion(question: Question) {
                EditQuestionActivity.startActivity(this@ResultActivity, test.id, question.id)
            }

            override fun onClickHome() {
                startActivity(Intent(this@ResultActivity, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                })
            }

            override fun onClickRetry() {
                AlertDialog.Builder(this@ResultActivity, R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.retry))
                        .setItems(resources.getStringArray(R.array.action_reload)) { _, which ->
                            when (which) {
                                0 -> {//全問やり直し

                                    sharedPreferenceManager.refine = false
                                    if (sharedPreferenceManager.manual) {
                                        SelfJudgePlayActivity.startActivity(this@ResultActivity, test.id, true)
                                    } else {
                                        PlayActivity.startActivity(this@ResultActivity, test.id, true)
                                    }
                                }

                                1 -> { //不正解のみやり直し

                                    if (questions.any { !it.isCorrect }) {

                                        sharedPreferenceManager.refine = true
                                        if (sharedPreferenceManager.manual) {
                                            SelfJudgePlayActivity.startActivity(this@ResultActivity, test.id, true)
                                        } else {
                                            PlayActivity.startActivity(this@ResultActivity, test.id, true)
                                        }
                                    } else {

                                        Toast.makeText(applicationContext, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

                                    }
                                }
                            }

                        }.show()
            }

            override fun onClickStudyPlus() {
                uploadStudyPlus(StudyRecord(
                        duration = (intent.getLongExtra("duration", 0) / 1000).toInt(),
                        amount = StudyRecordAmountTotal(questions.size),
                        comment = "${test.title} で勉強しました"))
            }

        }).also {
            it.setData(questions)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
        }

        setSupportActionBar(binding.toolbar)
        binding.toolbar.title = test.title
        createAd(binding.adView)

        binding.recyclerView.adapter = controller.adapter

        val record = StudyRecord(
                duration = (intent.getLongExtra("duration", 0) / 1000).toInt(),
                amount = StudyRecordAmountTotal(questions.size),
                comment = "${test.title} で勉強しました")

        when (sharedPreferenceManager.uploadStudyPlus) {
            resources.getStringArray(R.array.upload_setting_study_plus_values)[1] ->
                uploadStudyPlus(record)
            resources.getStringArray(R.array.upload_setting_study_plus_values)[2] -> {
                controller.isManualStudyPlus = true
                controller.setData(questions)
            }
        }
    }

    private fun uploadStudyPlus(record: StudyRecord) {

        if (!Studyplus.instance.isAuthenticated(baseContext)) return

        lifecycleScope.launch(Dispatchers.Default) {
            Studyplus.instance.postRecord(this@ResultActivity, record,
                    object : Studyplus.Companion.OnPostRecordListener {
                        override fun onResult(success: Boolean, recordId: Long?, throwable: Throwable?) {
                            if (success) {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Toast.makeText(baseContext, getString(R.string.msg_upload_study_plus), Toast.LENGTH_LONG).show()
                                    controller.isManualStudyPlus = false
                                    controller.setData(questions)
                                }
                            } else {
                                throwable?.apply {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Toast.makeText(baseContext, getString(R.string.msg_failed_upload_study_plus), Toast.LENGTH_LONG).show()
                                    }
                                    printStackTrace()
                                }
                            }
                        }
                    })
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this@ResultActivity, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
        super.onBackPressed()
    }

    companion object {

        fun startActivity(activity: Activity, id: Long, duration: Long) {
            val intent = Intent(activity, ResultActivity::class.java).apply {
                putExtra("id", id)
                putExtra("duration", duration)
            }
            activity.startActivity(intent)
        }
    }

}
