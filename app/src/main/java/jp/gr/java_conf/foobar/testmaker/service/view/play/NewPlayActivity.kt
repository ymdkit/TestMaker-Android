package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityNewPlayBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewPlayActivity : BaseActivity() {

    private val playViewModel: NewPlayViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    val index = 0

    private lateinit var test: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityNewPlayBinding>(this, R.layout.activity_new_play).apply {
            lifecycleOwner = this@NewPlayActivity
            viewModel = playViewModel
        }

        createAd(binding.adView)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
            supportActionBar?.setTitle(test.title)
        }

        playViewModel.selectedQuestion.value = test.questions[0]
    }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, NewPlayActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, id: Long, isRetry: Boolean) {
            val intent = Intent(activity, NewPlayActivity::class.java).apply {
                putExtra("id", id)
                putExtra("isRetry", isRetry)
            }
            activity.startActivity(intent)
        }
    }
}
