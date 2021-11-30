package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.group.GroupActivity
import jp.gr.java_conf.foobar.testmaker.service.view.preference.SettingsContainerFragment
import jp.gr.java_conf.foobar.testmaker.service.view.preference.SettingsFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.util.*


class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()
    private val logger: TestMakerLogger by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.offscreenPageLimit = 3
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = ViewPagerAdapter(
            this, listOf(
                HomeFragment(),
                SettingsFragment(),
                SettingsFragment(),
                SettingsContainerFragment()
            )
        )

        binding.bottomBar.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.page_home -> {
                    binding.viewPager.currentItem = 0
                    true
                }
                R.id.page_search -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.page_group -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                R.id.page_settings -> {
                    binding.viewPager.currentItem = 3
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            val pendingDynamicLinkData = withContext(Dispatchers.Default) {
                FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(intent).await()
            }

            pendingDynamicLinkData ?: return@launch
            val deepLink = pendingDynamicLinkData.link
            handleDynamicLink(deepLink.toString())
        }
    }

    private fun handleDynamicLink(link: String) {

        val regex = Regex("""(?<=https://testmaker-1cb29\.com/).*|(?<=https://ankimaker\.com/).*""")
        val result = regex.find(link, 0)

        result?.value?.let {

            val params = it.split("/")

            if (params.first() == "groups") {

                if (params.size != 2) return@let

                val groupId = params[1]
                GroupActivity.startActivityWithGroupId(this, groupId)

            } else {
                actionDownload(params[0])
            }
        }
    }

    private fun actionDownload(documentId: String) = lifecycleScope.launch {

        executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                viewModel.downloadTest(documentId)
            },
            onSuccess = {
                viewModel.convert(it)
                testViewModel.refresh()
                showToast(getString(R.string.msg_success_download_test, it.name))
                logger.logCreateTestEvent(it.name, CreateTestSource.DYNAMIC_LINKS.title)
            },
            onFailure = {
                showToast(getString(R.string.msg_failure_download_test))
            }
        )
    }

    private inner class ViewPagerAdapter(
        activity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    companion object {

        const val REQUEST_SIGN_IN = 12346

        fun startActivityWithClear(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity.startActivity(intent)
        }
    }
}
