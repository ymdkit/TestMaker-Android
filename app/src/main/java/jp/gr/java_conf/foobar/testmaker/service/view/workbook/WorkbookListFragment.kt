package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ui.R
import com.example.ui.core.*
import com.example.ui.core.item.ConfirmActionListItem
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.*
import com.example.usecase.utils.Resource
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.view.main.AnswerSettingDialogFragment
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WorkbookListFragment : Fragment() {

    private val adViewModel: AdViewModel by viewModels()
    private val workbookListViewModel: WorkbookListViewModel by viewModels()
    private val myWorkbookListViewModel: MyWorkbookListViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class, com.google.accompanist.pager.ExperimentalPagerApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {

                val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
                val scope = rememberCoroutineScope()
                TestMakerAndroidTheme {
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
                                            val uiState =
                                                workbookListViewModel.uiState.collectAsState()
                                            // todo 背景色の改善
                                            BottomDrawer(
                                                drawerState = drawerState,
                                                gesturesEnabled = drawerState.isExpanded,
                                                drawerContent = {
                                                    val workbook = uiState.value.selectedWorkbook
                                                    // fixme content の高さが確定していないとクラッシュするため、各々の部品で null チェックを行なっています
                                                    Text(
                                                        modifier = Modifier.padding(16.dp),
                                                        text = workbook?.name ?: ""
                                                    )
                                                    ClickableListItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.PlayArrow,
                                                                contentDescription = "answer workbook"
                                                            )
                                                        },
                                                        text = getString(R.string.play)
                                                    ) {
                                                        workbook ?: return@ClickableListItem
                                                        scope.launch { drawerState.close() }
                                                        AnswerSettingDialogFragment.newInstance(
                                                            workbookId = workbook.id,
                                                            workbookName = workbook.name
                                                        )
                                                            .show(childFragmentManager, "TAG")
                                                    }
                                                    ClickableListItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.Create,
                                                                contentDescription = "edit workbook"
                                                            )
                                                        },
                                                        text = getString(R.string.edit)
                                                    ) {
                                                        workbook ?: return@ClickableListItem
                                                        scope.launch {
                                                            drawerState.close()
                                                            findNavController().navigate(
                                                                WorkbookListFragmentDirections.actionHomeToListQuestion(
                                                                    workbook.id
                                                                )
                                                            )
                                                        }
                                                    }
                                                    ConfirmActionListItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.Delete,
                                                                contentDescription = "delete workbook"
                                                            )
                                                        },
                                                        label = getString(R.string.delete),
                                                        confirmMessage = stringResource(
                                                            id = R.string.message_delete_exam,
                                                            workbook?.name ?: ""
                                                        ),
                                                        confirmButtonText = stringResource(id = R.string.button_delete_confirm),
                                                    ) {
                                                        workbook ?: return@ConfirmActionListItem
                                                        workbookListViewModel.deleteWorkbook(
                                                            workbook
                                                        )
                                                        requireContext().showToast(getString(R.string.msg_success_delete_test))
                                                        scope.launch { drawerState.close() }
                                                    }
                                                    ClickableListItem(
                                                        icon = {
                                                            Icon(
                                                                imageVector = Icons.Filled.Share,
                                                                contentDescription = "share workbook"
                                                            )
                                                        },
                                                        text = getString(R.string.share)
                                                    ) {

                                                    }
                                                },
                                                content = {
                                                    Column {
                                                        Scaffold(
                                                            modifier = Modifier.weight(1f),
                                                            content = {
                                                                when (val state =
                                                                    uiState.value.resources) {
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
                                            )
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
                                                                                onClick = {}
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
                                                            // todo 問題集が空の時の対策
//                                                        val workbook =
//                                                            uiState.value.resources.getOrNull()?.workbookList?.firstOrNull()
//                                                                ?: return@FloatingActionButton
//                                                        findNavController().navigate(
//                                                            WorkbookListFragmentDirections.actionHomeToUploadWorkbook(
//                                                                workbookId = workbook.id
//                                                            )
//                                                        )
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
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adViewModel.setup()
        workbookListViewModel.setup()
        myWorkbookListViewModel.setup()
        workbookListViewModel.load()
        myWorkbookListViewModel.load()
    }
}
