package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityNewPlayBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionsBuilder
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class NewPlayActivity : BaseActivity() {

    private val playViewModel: NewPlayViewModel by viewModel {
        parametersOf(
                QuestionsBuilder(test.questions)
                        .retry(intent.hasExtra("isRetry"))
                        .startPosition(test.startPosition)
                        .mistakeOnly(sharedPreferenceManager.refine)
                        .shuffle(sharedPreferenceManager.random)
                        .limit(test.limit)
                        .build()
        )
    }
    private val testViewModel: TestViewModel by viewModel()

    private lateinit var test: Test

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityNewPlayBinding>(this, R.layout.activity_new_play).apply {
            lifecycleOwner = this@NewPlayActivity
            viewModel = playViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
            supportActionBar?.setTitle(test.title)
        }

        createAd(binding.adView)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        playViewModel.state.observeNonNull(this) {
            if (it == State.FINISH) {
                finish()
            }
        }

        playViewModel.loadNext()

    }

    override fun onStart() {
        super.onStart()
        playViewModel.selectedQuestion.observeNonNull(this) {
            when (it.type) {
                Constants.WRITE -> binding.editAnswer.requestFocus()
                Constants.COMPLETE -> binding.editAnswersFirst.editAnswer.requestFocus()
                else -> {
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
            when (item.itemId) {
                android.R.id.home -> {
                    finish()
                    true
                }
                else -> super.onOptionsItemSelected(item)
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
