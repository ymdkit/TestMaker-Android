package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.answer.AnswerSetting
import com.example.ui.answer.AnswerSettingViewModel
import com.example.ui.core.*
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.*
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkbookListFragment : Fragment() {

    private val args: WorkbookListFragmentArgs by navArgs()
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
                val myWorkbookListUiState by
                myWorkbookListViewModel.uiState.collectAsState()
                val drawerState =
                    rememberBottomDrawerState(BottomDrawerValue.Closed)

                val scope = rememberCoroutineScope()
                TestMakerAndroidTheme {
                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            when (val state =
                                workbookListUiState.workbookListDrawerState) {
                                is WorkbookListDrawerState.None -> {
                                    Spacer(modifier = Modifier.height(1.dp))
                                }
                                is WorkbookListDrawerState.OperateFolder -> {
                                    val folder = state.folder
                                    OperateFolder(
                                        folder = folder,
                                        onEdit = { newFolder ->
                                            scope.launch {
                                                drawerState.close()
                                                workbookListViewModel.updateFolder(newFolder)
                                                requireContext().showToast(getString(R.string.msg_success_update_folder))
                                            }
                                        },
                                        onDelete = {
                                            scope.launch {
                                                drawerState.close()
                                                workbookListViewModel.deleteFolder(folder)
                                                requireContext().showToast(getString(R.string.msg_success_delete_folder))
                                            }
                                        }
                                    )
                                }
                                is WorkbookListDrawerState.OperateWorkbook -> {
                                    val workbook = state.workbook
                                    OperateWorkbook(
                                        workbook = workbook,
                                        onAnswer = {
                                            scope.launch {
                                                if (!workbookListUiState.isShowAnswerSettingDialog) {
                                                    drawerState.close()
                                                }
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
                                        onShare = {
                                            workbookListViewModel.onShareWorkbookClicked(
                                                workbook
                                            )
                                        },
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
                                is WorkbookListDrawerState.UploadWorkbook -> {
                                    UploadWorkbook(
                                        workbookName = state.workbook.name,
                                        isUploading = workbookListUiState.isUploading,
                                        onUpload = { isPrivateUpload ->
                                            scope.launch {
                                                workbookListViewModel.onUploadWorkbookClicked(
                                                    workbook = state.workbook,
                                                    isPrivateUpload = isPrivateUpload
                                                )
                                            }
                                        }
                                    )
                                }
                                is WorkbookListDrawerState.OperateSharedWorkbook -> {
                                    OperateOwnSharedWorkbook(
                                        workbook = state.workbook,
                                        isDownloading = myWorkbookListUiState.isDownloading,
                                        onDownload = {
                                            scope.launch {
                                                myWorkbookListViewModel.onDownloadWorkbookClicked(
                                                    workbook = state.workbook
                                                )
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
                                        ) { page ->
                                            when (page) {
                                                0 -> {
                                                    Column {
                                                        Scaffold(
                                                            modifier = Modifier.weight(1f),
                                                            content = {
                                                                ResourceContent(
                                                                    resource = workbookListUiState.resources,
                                                                    onRetry = { workbookListViewModel.load() }) {

                                                                    val workbookList =
                                                                        it.workbookList
                                                                    val folderList =
                                                                        it.folderList

                                                                    var overscrollJob by remember {
                                                                        mutableStateOf<Job?>(
                                                                            null
                                                                        )
                                                                    }
                                                                    val dragDropListState =
                                                                        rememberDragDropListState(
                                                                            onMove = { from, to ->
                                                                                // note この部分で state にアクセスしても、初期状態のままなので2回目以降の入れ替わりが正しく動作しない
                                                                                workbookListViewModel.swapWorkbookOrFolder(
                                                                                    from,
                                                                                    to
                                                                                )
                                                                            })

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
                                                                        Column(
                                                                            modifier = Modifier.fillMaxHeight()
                                                                        ) {
                                                                            if (args.folderName.isNotEmpty()) {
                                                                                ListItem(
                                                                                    text = {
                                                                                        Text(
                                                                                            text = "/ ${args.folderName}",
                                                                                            fontSize = 12.sp
                                                                                        )
                                                                                    }
                                                                                )
                                                                            }
                                                                            LazyColumn(
                                                                                state = dragDropListState.lazyListState,
                                                                                modifier = Modifier
                                                                                    .fillMaxHeight()
                                                                                    .pointerInput(
                                                                                        Unit
                                                                                    ) {
                                                                                        detectDragGesturesAfterLongPress(
                                                                                            onDrag = { change, offset ->
                                                                                                change.consume()
                                                                                                dragDropListState.onDrag(
                                                                                                    offset
                                                                                                )

                                                                                                if (overscrollJob?.isActive == true)
                                                                                                    return@detectDragGesturesAfterLongPress

                                                                                                dragDropListState
                                                                                                    .checkForOverScroll()
                                                                                                    .takeIf { it != 0f }
                                                                                                    ?.let {
                                                                                                        overscrollJob =
                                                                                                            scope.launch {
                                                                                                                dragDropListState.lazyListState.scrollBy(
                                                                                                                    it
                                                                                                                )
                                                                                                            }
                                                                                                    }
                                                                                                    ?: run { overscrollJob?.cancel() }
                                                                                            },
                                                                                            onDragStart = { offset ->
                                                                                                dragDropListState.onDragStart(
                                                                                                    offset
                                                                                                )
                                                                                            },
                                                                                            onDragEnd = { dragDropListState.onDragInterrupted() },
                                                                                            onDragCancel = { dragDropListState.onDragInterrupted() }
                                                                                        )
                                                                                    }
                                                                            ) {
                                                                                itemsIndexed(
                                                                                    folderList
                                                                                ) { index, it ->
                                                                                    FolderListItem(
                                                                                        modifier = Modifier.composed {
                                                                                            val offsetOrNull =
                                                                                                dragDropListState.elementDisplacement.takeIf {
                                                                                                    index == dragDropListState.currentIndexOfDraggedItem
                                                                                                }
                                                                                            Modifier
                                                                                                .graphicsLayer {
                                                                                                    translationY =
                                                                                                        offsetOrNull
                                                                                                            ?: 0f
                                                                                                }
                                                                                        },
                                                                                        folder = it,
                                                                                        onClick = {
                                                                                            findNavController().navigate(
                                                                                                WorkbookListFragmentDirections.actionHomeToHomeQuestion(
                                                                                                    folderName = it.name
                                                                                                )
                                                                                            )
                                                                                        },
                                                                                        onMenuClicked = {
                                                                                            scope.launch {
                                                                                                workbookListViewModel.onFolderMenuClicked(
                                                                                                    it
                                                                                                )
                                                                                                drawerState.open()
                                                                                            }
                                                                                        }
                                                                                    )
                                                                                }
                                                                                itemsIndexed(
                                                                                    workbookList
                                                                                ) { index, it ->
                                                                                    WorkbookListItem(
                                                                                        modifier = Modifier.composed {
                                                                                            val offsetOrNull =
                                                                                                dragDropListState.elementDisplacement.takeIf {
                                                                                                    index + folderList.size == dragDropListState.currentIndexOfDraggedItem
                                                                                                }
                                                                                            Modifier
                                                                                                .graphicsLayer {
                                                                                                    translationY =
                                                                                                        offsetOrNull
                                                                                                            ?: 0f
                                                                                                }
                                                                                        },
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
                                                            },
                                                            floatingActionButton = {
                                                                FloatingActionButton(onClick = {
                                                                    findNavController().navigate(
                                                                        WorkbookListFragmentDirections.actionHomeToCreateWorkbook(
                                                                            folderName = args.folderName
                                                                        )
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
                                                    Column {
                                                        RequireAuthentication(
                                                            isLogin = myWorkbookListUiState.isLogin,
                                                            message = stringResource(id = R.string.msg_not_login_mypage),
                                                            onLogin = myWorkbookListViewModel::onUserCreated,
                                                            content = {
                                                                Scaffold(
                                                                    modifier = Modifier.weight(1f),
                                                                    content = {
                                                                        SwipeRefresh(
                                                                            state = rememberSwipeRefreshState(
                                                                                isRefreshing = myWorkbookListUiState.isRefreshing
                                                                            ),
                                                                            onRefresh = myWorkbookListViewModel::load
                                                                        ) {
                                                                            ResourceContent(
                                                                                resource = myWorkbookListUiState.myWorkbookList,
                                                                                onRetry = myWorkbookListViewModel::load
                                                                            ) {
                                                                                LazyColumn(
                                                                                    modifier = Modifier
                                                                                        .fillMaxHeight()
                                                                                ) {
                                                                                    it.forEach {
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
        workbookListViewModel.setup(folderName = args.folderName)
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
                        getString(R.string.msg_success_download_test)
                    )
                    val hostActivity = requireActivity() as? MainActivity
                    hostActivity?.navigateHomePage()
                }
                .launchIn(this)

            workbookListViewModel.deleteFolderEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_success_delete_folder))
                }
                .launchIn(this)

            workbookListViewModel.shareWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    startActivity(
                        actionSendIntent(
                            text = getString(R.string.msg_share_test, it.first, it.second)
                        )
                    )
                }
                .launchIn(this)

            workbookListViewModel.questionListEmptyEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_question_list_empty))
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
