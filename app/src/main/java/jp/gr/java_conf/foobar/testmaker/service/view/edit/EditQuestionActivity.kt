package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditQuestionActivity : BaseActivity() {

    private val editWriteQuestionViewModel: EditWriteQuestionViewModel by viewModel()
    private val editSelectQuestionViewModel: EditSelectQuestionViewModel by viewModel()
    private val editCompleteQuestionViewModel: EditCompleteQuestionViewModel by viewModel()
    private val editSelectCompleteQuestionViewModel: EditSelectCompleteQuestionViewModel by viewModel()
    private val editQuestionViewModel: EditQuestionViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditQuestionBinding>(this, R.layout.activity_edit_question).apply {
            lifecycleOwner = this@EditQuestionActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd(binding.adView)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewPager.offscreenPageLimit = 4
        binding.viewPager.adapter = ViewPagerAdapter(this, listOf(
                EditWriteQuestionFragment(),
                EditSelectQuestionFragment(),
                EditCompleteQuestionFragment(),
                EditSelectCompleteQuestionFragment()))

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            //todo: 出題形式のENUMを作ろう
            tab.text = listOf("記述", "選択", "完答", "選択完答")[position]
        }.attach()

        //todo: テストの受け渡し方法どうする
        editWriteQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)
        editSelectQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)
        editCompleteQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)
        editSelectCompleteQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)


        testViewModel.get(editWriteQuestionViewModel.testId).questions.find { it.id == intent.getLongExtra(ARGUMENT_QUESTION_ID, -1L) }?.let {
            //todo: 出題形式による条件分岐
            binding.tabLayout.getTabAt(it.type)?.select()
            editWriteQuestionViewModel.selectedQuestion = it
            editSelectQuestionViewModel.selectedQuestion = it
            editCompleteQuestionViewModel.selectedQuestion = it
            editSelectCompleteQuestionViewModel.selectedQuestion = it
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
        const val ARGUMENT_TEST_ID = "testId"
        const val ARGUMENT_QUESTION_ID = "questionId"

        fun startActivity(activity: Activity, testId: Long, questionId: Long) {
            val intent = Intent(activity, EditQuestionActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, testId)
                putExtra(ARGUMENT_QUESTION_ID, questionId)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, testId: Long) {
            val intent = Intent(activity, EditQuestionActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, testId)
            }
            activity.startActivity(intent)
        }
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity, private val fragments: List<Fragment>) : FragmentStateAdapter(activity) {
        //todo ENUMで管理
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
