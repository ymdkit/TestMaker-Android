package jp.gr.java_conf.foobar.testmaker.service.view.main

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

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
                R.id.page_upload_workbook,
                R.id.fragment_upload_group_test -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
                }
                else -> {
                    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                }
            }
        }

        lifecycleScope.launchWhenCreated {

            viewModel.downloadWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    showToast(getString(R.string.msg_success_download_test))
                }
                .launchIn(this)


            val pendingDynamicLinkData = withContext(Dispatchers.Default) {
                FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(intent).await()
            }

            pendingDynamicLinkData ?: return@launchWhenCreated
            val deepLink = pendingDynamicLinkData.link
            handleDynamicLink(deepLink.toString())
        }
    }

    fun navigateHomePage() {
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
                val workbookId = params[0]
                // todo 読み込み中の UI
                viewModel.downloadWorkbook(workbookId)
            }
        }
    }
}
