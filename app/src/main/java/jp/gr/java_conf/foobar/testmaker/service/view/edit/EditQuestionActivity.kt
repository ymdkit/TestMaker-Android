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
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditQuestionActivity : BaseActivity() {

    private val editQuestionViewModel: EditQuestionViewModel by viewModel()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditQuestionBinding>(this, R.layout.activity_edit_question).apply {
            lifecycleOwner = this@EditQuestionActivity
            viewModel = editQuestionViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd(binding.adView)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.viewPager.adapter = ViewPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = "OBJECT ${(position + 1)}"
        }.attach()

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
        fun startActivity(activity: Activity, testId: Long, questionId: Long) {
            val intent = Intent(activity, EditQuestionActivity::class.java).apply {
                putExtra("testId", testId)
                putExtra("questionId", questionId)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, testId: Long) {
            val intent = Intent(activity, EditQuestionActivity::class.java).apply {
                putExtra("testId", testId)
            }
            activity.startActivity(intent)
        }
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 4
        override fun createFragment(position: Int): Fragment = EditWriteQuestionFragment()
    }
}
