package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.*
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WorkbookListFragment : Fragment() {

    private val args: WorkbookListFragmentArgs by navArgs()
    private val adViewModel: AdViewModel by viewModels()
    private val workbookListViewModel: WorkbookListViewModel by viewModels()
    private val myWorkbookListViewModel: MyWorkbookListViewModel by viewModels()
    private val answerSettingViewModel: AnswerSettingViewModel by viewModels()

    @Inject
    lateinit var analytics: FirebaseAnalytics

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

                val launcher =
                    rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) {
                        if (it.resultCode == Activity.RESULT_OK) {
                            myWorkbookListViewModel.onUserCreated()
                        } else {
                            val response = it.idpResponse
                            context.showToast(
                                context.getString(
                                    com.example.ui.R.string.msg_failure_login,
                                    response?.error?.errorCode
                                )
                            )
                        }
                    }

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
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_UPDATE_FOLDER.eventName
                                            ) {}
                                            scope.launch {
                                                drawerState.close()
                                                workbookListViewModel.updateFolder(newFolder)
                                                requireContext().showToast(getString(R.string.msg_success_update_folder))
                                            }
                                        },
                                        onDelete = {
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_DELETE_FOLDER.eventName
                                            ) {}
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
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_PLAY_WORKBOOK.eventName
                                            ) {}
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
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_EDIT_WORKBOOK.eventName
                                            ) {}
                                            scope.launch {
                                                drawerState.close()
                                                if (findNavController().currentDestination?.id == R.id.page_home) {
                                                    findNavController().navigate(
                                                        WorkbookListFragmentDirections.actionHomeToListQuestion(
                                                            workbook.id
                                                        )
                                                    )
                                                }
                                            }
                                        },
                                        onShare = {
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_SHARE_WORKBOOK.eventName
                                            ) {}
                                            workbookListViewModel.onShareWorkbookClicked(
                                                workbook
                                            )
                                        },
                                        onDelete = {
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_DELETE_WORKBOOK.eventName
                                            ) {}
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
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_DOWNLOAD_UPLOADED_WORKBOOK.eventName
                                            ) {}
                                            scope.launch {
                                                myWorkbookListViewModel.onDownloadWorkbookClicked(
                                                    workbook = state.workbook
                                                )
                                            }
                                        },
                                        onShare = {
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_SHARE_UPLOADED_WORKBOOK.eventName
                                            ) {}
                                            scope.launch {
                                                myWorkbookListViewModel.onShareWorkbookClicked(
                                                    workbook = state.workbook
                                                )
                                                drawerState.close()
                                            }
                                        },
                                        onDelete = {
                                            analytics.logEvent(
                                                LogEvent.HOME_BUTTON_DELETE_UPLOADED_WORKBOOK.eventName
                                            ) {}
                                            scope.launch {
                                                myWorkbookListViewModel.onDeleteWorkbookClicked(
                                                    state.workbook
                                                )
                                                requireContext().showToast(getString(R.string.msg_delete_workbook))
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
                                content = { padding ->
                                    Column(
                                        modifier = Modifier.padding(padding)
                                    ) {
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
                                                        Scaffold(
                                                            modifier = Modifier.weight(1f),
                                                            content = { padding ->
                                                                ResourceContent(
                                                                    modifier = Modifier.padding(
                                                                        padding
                                                                    ),
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
                                                                                            analytics.logEvent(
                                                                                                LogEvent.HOME_ITEM_OPEN_FOLDER.eventName
                                                                                            ) {}
                                                                                            if (findNavController().currentDestination?.id == R.id.page_home) {
                                                                                                findNavController().navigate(
                                                                                                    WorkbookListFragmentDirections.actionHomeToHomeQuestion(
                                                                                                        folderName = it.name
                                                                                                    )
                                                                                                )
                                                                                            }
                                                                                        },
                                                                                        onMenuClicked = {
                                                                                            analytics.logEvent(
                                                                                                LogEvent.HOME_ITEM_OPERATE_FOLDER.eventName
                                                                                            ) {}
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
                                                                                            analytics.logEvent(
                                                                                                LogEvent.HOME_ITEM_OPERATE_WORKBOOK.eventName
                                                                                            ) {}
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
                                                                if (workbookListUiState.showingRequestAuthDialog) {
                                                                    AlertDialog(
                                                                        onDismissRequest = workbookListViewModel::onDismissRequestAuthDialog,
                                                                        text = {
                                                                            Text(
                                                                                text = stringResource(
                                                                                    id = R.string.msg_login_for_share
                                                                                )
                                                                            )
                                                                        },
                                                                        buttons = {
                                                                            Row(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .padding(8.dp),
                                                                                horizontalArrangement = Arrangement.End
                                                                            ) {
                                                                                TextButton(onClick = workbookListViewModel::onDismissRequestAuthDialog) {
                                                                                    Text(
                                                                                        text = stringResource(
                                                                                            id = R.string.cancel
                                                                                        ),
                                                                                        color = MaterialTheme.colors.onBackground
                                                                                    )
                                                                                }
                                                                                Spacer(
                                                                                    modifier = Modifier.width(
                                                                                        4.dp
                                                                                    )
                                                                                )
                                                                                TextButton(onClick = {
                                                                                    val intent =
                                                                                        AuthUI.getInstance()
                                                                                            .createSignInIntentBuilder()
                                                                                            .setAvailableProviders(
                                                                                                arrayListOf(
                                                                                                    AuthUI.IdpConfig.EmailBuilder()
                                                                                                        .build(),
                                                                                                    AuthUI.IdpConfig.GoogleBuilder()
                                                                                                        .build()
                                                                                                )
                                                                                            )
                                                                                            .setTosAndPrivacyPolicyUrls(
                                                                                                "https://ankimaker.com/terms",
                                                                                                "https://ankimaker.com/privacy"
                                                                                            )
                                                                                            .build()

                                                                                    launcher.launch(
                                                                                        intent
                                                                                    )
                                                                                    workbookListViewModel.onDismissRequestAuthDialog()
                                                                                }) {
                                                                                    Text(
                                                                                        text = stringResource(
                                                                                            id = R.string.button_login_confirm,
                                                                                        ),
                                                                                        color = MaterialTheme.colors.onBackground
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    )
                                                                }
                                                            },
                                                            floatingActionButton = {
                                                                FloatingActionButton(onClick = {
                                                                    if (findNavController().currentDestination?.id == R.id.page_home) {
                                                                        analytics.logEvent(
                                                                            LogEvent.HOME_BUTTON_CREATE_WORKBOOK.eventName
                                                                        ) {}
                                                                        findNavController().navigate(
                                                                            WorkbookListFragmentDirections.actionHomeToCreateWorkbook(
                                                                                folderName = args.folderName
                                                                            )
                                                                        )
                                                                    }
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
                                                                    content = { padding ->
                                                                        SwipeRefresh(
                                                                            modifier = Modifier.padding(
                                                                                padding
                                                                            ),
                                                                            state = rememberSwipeRefreshState(
                                                                                isRefreshing = myWorkbookListUiState.isRefreshing
                                                                            ),
                                                                            onRefresh = {
                                                                                analytics.logEvent(
                                                                                    LogEvent.HOME_REFRESH_UPLOADED_WORKBOOK.eventName
                                                                                ) {}
                                                                                myWorkbookListViewModel.load()
                                                                            }
                                                                        ) {
                                                                            ResourceContent(
                                                                                resource = myWorkbookListUiState.myWorkbookList,
                                                                                onRetry = myWorkbookListViewModel::load
                                                                            ) {

                                                                                if (it.isEmpty()) {
                                                                                    Column(
                                                                                        modifier = Modifier
                                                                                            .fillMaxSize()
                                                                                            .padding(
                                                                                                16.dp
                                                                                            ),
                                                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                                                        verticalArrangement = Arrangement.Center
                                                                                    ) {
                                                                                        Text(
                                                                                            text = stringResource(
                                                                                                id = R.string.empty_uploaded_test
                                                                                            )
                                                                                        )
                                                                                    }
                                                                                } else {
                                                                                    LazyColumn(
                                                                                        modifier = Modifier
                                                                                            .fillMaxHeight()
                                                                                    ) {
                                                                                        it.forEach {
                                                                                            item {
                                                                                                SharedWorkbookListItem(
                                                                                                    workbook = it,
                                                                                                    onClick = {
                                                                                                        analytics.logEvent(
                                                                                                            LogEvent.HOME_ITEM_OPERATE_UPLOADED_WORKBOOK.eventName
                                                                                                        ) {}
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
                                                                        }
                                                                    },
                                                                    floatingActionButton = {
                                                                        FloatingActionButton(onClick = {
                                                                            analytics.logEvent(
                                                                                LogEvent.HOME_BUTTON_UPLOAD_WORKBOOK.eventName
                                                                            ) {}
                                                                            if (findNavController().currentDestination?.id == R.id.page_home) {
                                                                                findNavController().navigate(
                                                                                    WorkbookListFragmentDirections.actionHomeToUploadWorkbook()
                                                                                )
                                                                            }
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
                    if (findNavController().currentDestination?.id == R.id.page_home) {
                        findNavController().navigate(
                            WorkbookListFragmentDirections.actionHomeToAnswerWorkbook(
                                workbookId = it.workbookId,
                                isRetry = it.isRetry
                            )
                        )
                    }
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

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.HOME_SCREEN_OPEN.eventName)
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
