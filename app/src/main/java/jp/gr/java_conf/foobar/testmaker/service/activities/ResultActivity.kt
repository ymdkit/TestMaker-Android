package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.ResultAdapter
import kotlinx.android.synthetic.main.activity_result.*

class ResultActivity : BaseActivity() {
    private lateinit var resultAdapter: ResultAdapter
    internal var testId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        sendScreen("ResultActivity")

        testId = intent.getLongExtra("testId", -1)

        setSupportActionBar(toolbar)

        container.addView(createAd())

        val questions = realmController.getQuestionsSolved(testId)

        resultAdapter = ResultAdapter(this, realmController, testId)

        recyclerview.layoutManager = LinearLayoutManager(applicationContext)
        recyclerview.setHasFixedSize(true) // アイテムは固定サイズ
        recyclerview.adapter = resultAdapter

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
                            0 -> //全問やり直し

                                startActivity(i)

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

    }

    override fun onBackPressed() {

        startActivity(Intent(this@ResultActivity, MainActivity::class.java))

        super.onBackPressed()
    }

}
