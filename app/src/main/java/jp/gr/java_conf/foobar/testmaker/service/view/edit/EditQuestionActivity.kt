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

    private val editQuestionViewModel: EditQuestionViewModel by viewModel()
    private val editSelectQuestionViewModel: EditSelectQuestionViewModel by viewModel()
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
        binding.viewPager.adapter = ViewPagerAdapter(this, listOf(EditWriteQuestionFragment(), EditSelectQuestionFragment()))

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            //todo: 出題形式のENUMを作ろう
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()

        //todo: テストの受け渡し方法どうする
        editQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)
        editSelectQuestionViewModel.testId = intent.getLongExtra(ARGUMENT_TEST_ID, -1L)


        testViewModel.get(editQuestionViewModel.testId).questions.find { it.id == intent.getLongExtra(ARGUMENT_QUESTION_ID, -1L) }?.let {
            //todo: 出題形式による条件分岐
            binding.tabLayout.getTabAt(it.type)?.select()
            editQuestionViewModel.selectedQuestion = it
            editSelectQuestionViewModel.selectedQuestion = it
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
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}
