package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.ui.core.showToast
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val testViewModel: TestViewModel by viewModels()

    @Inject
    lateinit var logger: TestMakerLogger

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_main) as NavHostFragment).navController
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.bottomBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.page_answer_workbook,
                R.id.page_create_workbook,
                R.id.page_upload_workbook,
                R.id.page_edit_workbook,
                R.id.page_list_question,
                R.id.page_create_folder,
                R.id.page_create_question,
                R.id.page_edit_question,
                R.id.page_answer_result -> {
                    binding.bottomBar.isGone = true
                }
                else -> {
                    binding.bottomBar.isGone = false
                }
            }

            when (destination.id) {
                R.id.page_settings,
                R.id.page_upload_workbook -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                }
                else -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
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

    fun navigateHomePage() {
        testViewModel.refresh()
        binding.bottomBar.selectedItemId = R.id.page_home
        navController.navigate(R.id.action_global_page_home)
    }

    private fun navigateGroupPage(groupId: String) {
        binding.bottomBar.selectedItemId = R.id.page_group
        // todo
//        navController.navigate(
//            GroupListFragmentDirections.actionGroupListToGroupDetail(
//                groupId = groupId
//            )
//        )
    }

    private fun handleDynamicLink(link: String) {

        val regex = Regex("""(?<=https://testmaker-1cb29\.com/).*|(?<=https://ankimaker\.com/).*""")
        val result = regex.find(link, 0)

        result?.value?.let {

            val params = it.split("/")

            if (params.first() == "groups") {

                if (params.size != 2) return@let

                navigateGroupPage(groupId = params[1])

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
