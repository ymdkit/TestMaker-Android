package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.example.ui.core.showToast
import com.example.ui.home.MainViewModel
import com.example.ui.home.NavigationPage
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.view.group.GroupWorkbookListFragmentDirections
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

    private val navigationItemList by lazy {
        listOf(
            NavigationItem(
                page = NavigationPage.HOME,
                resId = R.id.page_home,
                icon = Icons.Default.Home,
                contentDescription = "Home",
                text = getString(R.string.title_page_home),
            ),
            NavigationItem(
                page = NavigationPage.SEARCH,
                resId = R.id.page_search,
                icon = Icons.Default.Search,
                contentDescription = "Search",
                text = getString(R.string.title_page_search),
            ),
            NavigationItem(
                page = NavigationPage.GROUP,
                resId = R.id.page_group,
                icon = Icons.Default.Group,
                contentDescription = "Group",
                text = getString(R.string.title_page_group),
            ),
            NavigationItem(
                page = NavigationPage.SETTING,
                resId = R.id.page_settings,
                icon = Icons.Default.Settings,
                contentDescription = "Settings",
                text = getString(R.string.title_page_settings),
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.composeBottomBar.setContent {
            TestMakerAndroidTheme {
                Surface {
                    val uiState by viewModel.uiState.collectAsState()

                    if (uiState.showingBottomBar) {
                        BottomAppBar(
                            elevation = 0.dp,
                            backgroundColor = Color.Transparent
                        ) {
                            navigationItemList.forEach {
                                BottomNavigationItem(
                                    selected = uiState.selectedBottomBarPage == it.page,
                                    onClick = {
                                        viewModel.onSelectedBottomBarPageChanged(it.page)
                                        navigate(it.page)
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = it.icon,
                                            contentDescription = it.contentDescription
                                        )
                                    },
                                    label = { Text(text = it.text) }
                                )
                            }
                        }
                    }
                }
            }
        }

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
                    viewModel.onShowingBottomBarChanged(false)
                }
                else -> {
                    viewModel.onShowingBottomBarChanged(true)
                }
            }

            when (destination.id) {
                R.id.page_home,
                R.id.page_settings,
                R.id.page_upload_workbook,
                R.id.fragment_upload_group_test,
                R.id.page_search,
                R.id.page_group -> {
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

            viewModel.joinGroupEvent
                .receiveAsFlow()
                .onEach {
                    navigate(NavigationPage.GROUP)
                    navController.navigate(
                        GroupWorkbookListFragmentDirections.actionGlobalPageGroupDetail(
                            groupId = it
                        )
                    )
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
        navigate(NavigationPage.HOME)
    }

    private fun navigate(page: NavigationPage) {
        val dest = navigationItemList.firstOrNull { it.page == page } ?: return
        navController.navigate(dest.resId)
        viewModel.onSelectedBottomBarPageChanged(dest.page)
    }

    private fun handleDynamicLink(link: String) {

        val regex = Regex("""(?<=https://testmaker-1cb29\.com/).*|(?<=https://ankimaker\.com/).*""")
        val result = regex.find(link, 0)

        result?.value?.let {

            val params = it.split("/")

            if (params.first() == "groups") {

                if (params.size != 2) return@let

                viewModel.joinGroup(groupId = params[1])

            } else {
                val workbookId = params[0]
                // todo 読み込み中の UI （Compose 化した後）
                viewModel.downloadWorkbook(workbookId)
            }
        }
    }

    data class NavigationItem(
        val page: NavigationPage,
        val resId: Int,
        val icon: ImageVector,
        val contentDescription: String,
        val text: String,
    )
}

