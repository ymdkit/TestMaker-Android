package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.core.utils.Resource
import com.example.ui.R
import com.example.ui.answer.AnswerSetting
import com.example.ui.answer.AnswerSettingViewModel
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.TestMakerTopAppBar
import com.example.ui.core.showToast
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.*
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkbookListFragment : Fragment() {

    private val adViewModel: AdViewModel by viewModels()
    private val workbookListViewModel: WorkbookListViewModel by viewModels()
    private val myWorkbookListViewModel: MyWorkbookListViewModel by viewModels()
    private val answerSettingViewModel: AnswerSettingViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val workbookListUiState by workbookListViewModel.uiState.collectAsState()
                val drawerState =
                    rememberBottomDrawerState(BottomDrawerValue.Closed)

                val scope = rememberCoroutineScope()
                TestMakerAndroidTheme {
                    // todo 背景色の改善
                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            when (val state =
                                workbookListUiState.workbookListDrawerState) {
                                is WorkbookListDrawerState.None -> {
                                    Spacer(modifier = Modifier.height(1.dp))
                                }
                                is WorkbookListDrawerState.OperateWorkbook -> {
                                    val workbook = state.workbook
                                    OperateWorkbook(
                                        workbook = workbook,
                                        onAnswer = {
                                            scope.launch {
                                                workbookListViewModel.onAnswerWorkbookClicked(
                                                    workbook
                                                )
                                            }
                                        },
                                        onEdit = {
                                            scope.launch {
                                                drawerState.close()
                                                findNavController().navigate(
                                                    WorkbookListFragmentDirections.actionHomeToListQuestion(
                                                        workbook.id
                                                    )
                                                )
                                            }
                                        },
                                        onShare = { /*TODO*/ },
                                        onDelete = {
                                            scope.launch {
                                                workbookListViewModel.deleteWorkbook(
                                                    workbook
                                                )
                                                requireContext().showToast(
                                                    getString(
                                                        R.string.msg_success_delete_test
                                                    )
                                                )
                                                drawerState.close()
                                            }
                                        })
                                }
                                is WorkbookListDrawerState.OperateSharedWorkbook -> {
                                    OperateOwnSharedWorkbook(
                                        workbook = state.workbook,
                                        onDownload = {
                                            scope.launch {
                                                myWorkbookListViewModel.onDownloadWorkbookClicked(
                                                    workbook = state.workbook
                                                )
                                                drawerState.close()
                                            }
                                        },
                                        onShare = {
                                            scope.launch {
                                                myWorkbookListViewModel.onShareWorkbookClicked(
                                                    workbook = state.workbook
                                                )
                                                drawerState.close()
                                            }
                                        },
                                        onDelete = {
                                            scope.launch {
                                                myWorkbookListViewModel.onDeleteWorkbookClicked(
                                                    state.workbook
                                                )
                                                drawerState.close()
                                            }
                                        }
                                    )
                                }
                                is WorkbookListDrawerState.AnswerSetting -> {
                                    AnswerSetting(
                                        workbookName = state.workbook.name,
                                        onStartButtonClicked = {
                                            scope.launch {
                                                drawerState.close()
                                                workbookListViewModel.onStartAnswerClicked(
                                                    state.workbook.id
                                                )
                                            }
                                        },
                                        answerSettingViewModel = answerSettingViewModel
                                    )
                                }
                            }
                        },
                        content = {
                            Scaffold(
                                topBar = {
                                    TestMakerTopAppBar(title = stringResource(id = R.string.app_name))
                                },
                                content = {
                                    Column {
                                        val tabList = listOf(
                                            stringResource(id = R.string.tab_local),
                                            stringResource(id = R.string.tab_remote)
                                        )
                                        val pagerState = rememberPagerState(
                                            initialPage = 0
                                        )
                                        TabRow(
                                            selectedTabIndex = pagerState.currentPage,
                                            backgroundColor = Color.Transparent
                                        ) {
                                            tabList.forEachIndexed { index, it ->
                                                Tab(
                                                    selected = pagerState.currentPage == index,
                                                    selectedContentColor = MaterialTheme.colors.onBackground,
                                                    unselectedContentColor = MaterialTheme.colors.onBackground.copy(
                                                        alpha = ContentAlpha.medium
                                                    ),
                                                    onClick = {
                                                        scope.launch {
                                                            pagerState.animateScrollToPage(index)
                                                        }
                                                    },
                                                    text = { Text(it) },
                                                )
                                            }
                                        }
                                        HorizontalPager(
                                            modifier = Modifier
                                                .weight(1f),
                                            state = pagerState,
                                            count = tabList.size
                                        ) { index ->
                                            when (index) {
                                                0 -> {
                                                    Column {
                                                        Scaffold(
                                                            modifier = Modifier.weight(1f),
                                                            content = {
                                                                when (val state =
                                                                    workbookListUiState.resources) {
                                                                    is Resource.Success -> {
                                                                        val workbookList =
                                                                            state.value.workbookList
                                                                        val folderList =
                                                                            state.value.folderList
                                                                        if (workbookList.isEmpty() && folderList.isEmpty()) {
                                                                            Column(
                                                                                modifier = Modifier.fillMaxSize(),
                                                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                                                verticalArrangement = Arrangement.Center
                                                                            ) {
                                                                                Text(
                                                                                    text = stringResource(
                                                                                        id = R.string.empty_test
                                                                                    )
                                                                                )
                                                                            }
                                                                        } else {
                                                                            LazyColumn(
                                                                                modifier = Modifier
                                                                                    .fillMaxHeight()
                                                                            ) {
                                                                                item {
                                                                                    Spacer(
                                                                                        modifier = Modifier.height(
                                                                                            8.dp
                                                                                        )
                                                                                    )
                                                                                }
                                                                                if (folderList.isNotEmpty()) {
                                                                                    item {
                                                                                        Text(
                                                                                            modifier = Modifier.padding(
                                                                                                horizontal = 16.dp
                                                                                            ),
                                                                                            text = stringResource(
                                                                                                id = R.string.folder
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                                folderList.forEach {
                                                                                    item {
                                                                                        FolderListItem(
                                                                                            folder = it
                                                                                        )
                                                                                    }
                                                                                }
                                                                                if (workbookList.isNotEmpty()) {
                                                                                    item {
                                                                                        Text(
                                                                                            modifier = Modifier.padding(
                                                                                                horizontal = 16.dp
                                                                                            ),
                                                                                            text = stringResource(
                                                                                                id = R.string.workbook
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                }
                                                                                workbookList.forEach {
                                                                                    item {
                                                                                        WorkbookListItem(
                                                                                            workbook = it,
                                                                                            onClick = {
                                                                                                scope.launch {
                                                                                                    drawerState.expand()
                                                                                                }
                                                                                                workbookListViewModel.onWorkbookClicked(
                                                                                                    it
                                                                                                )
                                                                                            }
                                                                                        )
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        // todo
                                                                    }
                                                                }
                                                            },
                                                            floatingActionButton = {
                                                                FloatingActionButton(onClick = {
                                                                    findNavController().navigate(
                                                                        WorkbookListFragmentDirections.actionHomeToCreateWorkbook()
                                                                    )
                                                                }) {
                                                                    Icon(
                                                                        Icons.Filled.Add,
                                                                        contentDescription = "create workbook"
                                                                    )
                                                                }
                                                            }
                                                        )
                                                        AdView(viewModel = adViewModel)
                                                    }
                                                }
                                                1 -> {
                                                    val uiState =
                                                        myWorkbookListViewModel.uiState.collectAsState()

                                                    Column {
                                                        Scaffold(
                                                            modifier = Modifier.weight(1f),
                                                            content = {
                                                                when (val state =
                                                                    uiState.value.myWorkbookList) {
                                                                    is Resource.Success -> {
                                                                        LazyColumn(
                                                                            modifier = Modifier
                                                                                .fillMaxHeight()
                                                                        ) {
                                                                            item {
                                                                                Spacer(
                                                                                    modifier = Modifier.height(
                                                                                        8.dp
                                                                                    )
                                                                                )
                                                                            }
                                                                            if (state.value.isNotEmpty()) {
                                                                                item {
                                                                                    Text(
                                                                                        modifier = Modifier.padding(
                                                                                            horizontal = 16.dp
                                                                                        ),
                                                                                        text = stringResource(
                                                                                            id = R.string.workbook
                                                                                        )
                                                                                    )
                                                                                }
                                                                            }
                                                                            state.value.forEach {
                                                                                item {
                                                                                    SharedWorkbookListItem(
                                                                                        workbook = it,
                                                                                        onClick = {
                                                                                            scope.launch {
                                                                                                workbookListViewModel.onSharedWorkbookClicked(
                                                                                                    it
                                                                                                )
                                                                                                drawerState.open()
                                                                                            }
                                                                                        }
                                                                                    )

                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                    else -> {
                                                                        Box(
                                                                            modifier = Modifier.fillMaxSize(),
                                                                            contentAlignment = Alignment.Center
                                                                        ) {
                                                                            CircularProgressIndicator()
                                                                        }
                                                                    }
                                                                }
                                                            },
                                                            floatingActionButton = {
                                                                FloatingActionButton(onClick = {
                                                                    findNavController().navigate(
                                                                        WorkbookListFragmentDirections.actionHomeToUploadWorkbook()
                                                                    )
                                                                }) {
                                                                    Icon(
                                                                        Icons.Filled.CloudUpload,
                                                                        contentDescription = "upload workbook"
                                                                    )
                                                                }
                                                            }
                                                        )
                                                        AdView(viewModel = adViewModel)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adViewModel.setup()
        workbookListViewModel.setup()
        myWorkbookListViewModel.setup()
        answerSettingViewModel.setup()
        workbookListViewModel.load()
        myWorkbookListViewModel.load()

        lifecycleScope.launchWhenCreated {

            workbookListViewModel.navigateToAnswerWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    findNavController().navigate(
                        WorkbookListFragmentDirections.actionHomeToAnswerWorkbook(
                            workbookId = it.workbookId,
                            isRetry = it.isRetry
                        )
                    )
                }
                .launchIn(this)

            myWorkbookListViewModel.shareWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    startActivity(
                        actionSendIntent(
                            text = getString(
                                jp.gr.java_conf.foobar.testmaker.service.R.string.msg_share_test,
                                it.first,
                                it.second
                            )
                        )
                    )
                }
                .launchIn(this)

            myWorkbookListViewModel.downloadWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(
                        getString(
                            jp.gr.java_conf.foobar.testmaker.service.R.string.msg_success_download_test,
                            it
                        )
                    )
                    val hostActivity = requireActivity() as? MainActivity
                    hostActivity?.navigateHomePage()
                }
                .launchIn(this)
        }
    }

    // todo 共通化
    private fun actionSendIntent(text: String) =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_TEXT,
                text
            )
            type = "text/plain"
        }, null)
}
