package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.utils.Resource
import com.example.ui.core.*
import com.example.ui.core.item.ConfirmActionListItem
import com.example.ui.group.GroupWorkbookListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class GroupWorkbookListFragment : Fragment() {

    private val args: GroupWorkbookListFragmentArgs by navArgs()

    private val groupWorkbookListViewModel: GroupWorkbookListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by groupWorkbookListViewModel.uiState.collectAsState()
                val drawerState = rememberBottomDrawerState(BottomDrawerValue.Closed)
                val scope = rememberCoroutineScope()

                TestMakerAndroidTheme {
                    BottomDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = !drawerState.isClosed,
                        drawerContent = {
                            val workbook = uiState.selectedSharedWorkbook

                            if (workbook != null) {
                                ListItem(
                                    text = {
                                        Text(
                                            text = workbook.name,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                )
                                ListItem(
                                    text = {
                                        Text(text = stringResource(id = R.string.workbook_post_user))
                                    },
                                    secondaryText = {
                                        Text(
                                            text = workbook.userName
                                        )
                                    }
                                )
                                if (workbook.comment.isNotEmpty()) {
                                    ListItem(
                                        text = {
                                            Text(text = stringResource(id = R.string.overview))
                                        },
                                        secondaryText = {
                                            Text(
                                                text = workbook.comment
                                            )
                                        }
                                    )
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Download,
                                            contentDescription = "download"
                                        )
                                    },
                                    text = stringResource(id = R.string.download)
                                ) {
                                    groupWorkbookListViewModel.onDownloadWorkbookClicked(workbook = workbook)
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.History,
                                            contentDescription = "history"
                                        )
                                    },
                                    text = stringResource(id = R.string.history)
                                ) {
                                    scope.launch {
                                        drawerState.close()
                                        findNavController().navigate(
                                            GroupWorkbookListFragmentDirections.actionGroupDetailToHistoryTest(
                                                documentId = workbook.id
                                            )
                                        )
                                    }
                                }
                                ClickableListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Share,
                                            contentDescription = "share"
                                        )
                                    },
                                    text = stringResource(id = R.string.share)
                                ) {
                                    scope.launch {
                                        groupWorkbookListViewModel.onShareWorkbookClicked(workbook = workbook)
                                        drawerState.close()
                                    }
                                }
                                // todo 持ち主かどうかの確認
                                ConfirmActionListItem(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Filled.Delete,
                                            contentDescription = "delete"
                                        )
                                    },
                                    label = stringResource(id = R.string.delete),
                                    confirmMessage = stringResource(
                                        id = com.example.ui.R.string.message_delete,
                                        workbook.name
                                    ),
                                    confirmButtonText = stringResource(id = R.string.delete),
                                    onConfirmed = {
                                        scope.launch {
                                            groupWorkbookListViewModel.onDeleteWorkbookClicked(
                                                workbook
                                            )
                                            drawerState.close()
                                        }
                                    }
                                )
                            } else {
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }
                    ) {
                        Column {
                            Scaffold(
                                modifier = Modifier.weight(1f),
                                topBar = {
                                    TestMakerTopAppBar(
                                        navigationIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.ArrowBack,
                                                contentDescription = "Back",
                                                modifier = Modifier
                                                    .padding(16.dp)
                                                    .clickable {
                                                        findNavController().popBackStack()
                                                    }
                                            )
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = groupWorkbookListViewModel::onInviteButtonClicked,
                                                content = {
                                                    Icon(
                                                        imageVector = Icons.Filled.GroupAdd,
                                                        contentDescription = "invite"
                                                    )
                                                }
                                            )
                                            IconButton(onClick = groupWorkbookListViewModel::onMenuToggleButtonClicked) {
                                                Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = "more"
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = uiState.showingMenu,
                                                onDismissRequest = groupWorkbookListViewModel::onMenuToggleButtonClicked
                                            ) {
                                                if (uiState.isOwner) {
                                                    DropdownMenuItem(
                                                        onClick = groupWorkbookListViewModel::onEditGroupButtonClicked
                                                    ) {
                                                        Text(stringResource(id = R.string.rename_group))
                                                    }
                                                    ConfirmActionDropDownMenu(
                                                        label = stringResource(id = R.string.delete_group),
                                                        // todo グループ名の編集 → グループの削除の順で作業した場合に最新のグループ名が反映されない問題
                                                        confirmMessage = stringResource(
                                                            id = R.string.msg_delete_group,
                                                            uiState.group.getOrNull()?.name ?: ""
                                                        ),
                                                        confirmButtonText = stringResource(id = R.string.delete),
                                                        onConfirmed = groupWorkbookListViewModel::onDeleteGroupButtonClicked
                                                    )
                                                } else {
                                                    ConfirmActionDropDownMenu(
                                                        label = stringResource(id = R.string.exit_group),
                                                        // todo グループ名の編集 → グループの退出の順で作業した場合に最新のグループ名が反映されない問題
                                                        confirmMessage = stringResource(
                                                            id = R.string.msg_exit_group,
                                                            uiState.group.getOrNull()?.name ?: ""
                                                        ),
                                                        confirmButtonText = stringResource(id = R.string.delete),
                                                        onConfirmed = groupWorkbookListViewModel::onExitGroupButtonClicked
                                                    )
                                                }
                                            }
                                        },
                                        title = stringResource(id = R.string.group_detail_fragment_label)
                                    )
                                },
                            ) {
                                Scaffold(
                                    content = {
                                        RequireAuthentication(
                                            isLogin = uiState.isLogin,
                                            onLogin = groupWorkbookListViewModel::onUserCreated
                                        ) {
                                            SwipeRefresh(state = rememberSwipeRefreshState(
                                                isRefreshing = uiState.isRefreshing
                                            ), onRefresh = {
                                                groupWorkbookListViewModel.load()
                                            }) {
                                                when (val state = uiState.workbookList) {
                                                    is Resource.Success -> {
                                                        if (state.value.isNotEmpty()) {
                                                            LazyColumn(
                                                                modifier = Modifier.fillMaxHeight()
                                                            ) {
                                                                item {
                                                                    Text(
                                                                        modifier = Modifier.padding(
                                                                            16.dp
                                                                        ),
                                                                        text = stringResource(
                                                                            id = R.string.workbook
                                                                        )
                                                                    )
                                                                }
                                                                state.value.forEach {
                                                                    item {
                                                                        ClickableListItem(
                                                                            icon = {
                                                                                Icon(
                                                                                    modifier = Modifier
                                                                                        .size(40.dp)
                                                                                        .padding(8.dp),
                                                                                    imageVector = Icons.Filled.Description,
                                                                                    // todo 動的に色を変更
                                                                                    tint = MaterialTheme.colors.primary,
                                                                                    contentDescription = "workbook",
                                                                                )
                                                                            },
                                                                            text = it.name,
                                                                            secondaryText = stringResource(
                                                                                id = R.string.num_questions,
                                                                                it.questionListCount
                                                                            ),
                                                                            onClick = {
                                                                                scope.launch {
                                                                                    groupWorkbookListViewModel.onWorkbookClicked(
                                                                                        workbook = it
                                                                                    )
                                                                                    drawerState.expand()
                                                                                }

                                                                            }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        } else {
                                                            Box(
                                                                modifier = Modifier
                                                                    .fillMaxSize()
                                                                    .padding(16.dp),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Text(text = stringResource(id = R.string.empty_uploaded_test))
                                                            }
                                                        }
                                                    }
                                                    else -> {
                                                        // do nothing
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    floatingActionButton = {
                                        FloatingActionButton(onClick = {
                                            findNavController().navigate(
                                                GroupWorkbookListFragmentDirections.actionGroupDetailToUploadTest(
                                                    groupId = args.groupId
                                                )
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
                            AdView(viewModel = adViewModel)
                        }
                        if (uiState.showingEditGroupDialog) {
                            EditTextDialog(
                                title = stringResource(id = R.string.title_rename_group),
                                value = uiState.editingGroupName,
                                onValueChanged = groupWorkbookListViewModel::onGroupNameChanged,
                                placeholder = stringResource(id = R.string.hint_group_name),
                                onDismiss = groupWorkbookListViewModel::onCancelEditGroupButtonClicked,
                                onSubmit = groupWorkbookListViewModel::onUpdateGroup
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupWorkbookListViewModel.setup(groupId = args.groupId)
        groupWorkbookListViewModel.load()

        lifecycleScope.launchWhenCreated {
            groupWorkbookListViewModel.inviteGroupEvent
                .receiveAsFlow()
                .onEach {
                    startActivity(
                        actionSendIntent(
                            text = getString(R.string.msg_invite_group, it.first, it.second)
                        )
                    )
                }
                .launchIn(this)

            groupWorkbookListViewModel.exitGroupEvent
                .receiveAsFlow()
                .onEach {
                    findNavController().popBackStack()
                }
                .launchIn(this)

            groupWorkbookListViewModel.shareWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    startActivity(
                        actionSendIntent(
                            text = getString(R.string.msg_share_test, it.first, it.second)
                        )
                    )
                }
                .launchIn(this)

            groupWorkbookListViewModel.downloadWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_success_download_test))
                    val hostActivity = requireActivity() as? MainActivity
                    hostActivity?.navigateHomePage()
                }
                .launchIn(this)
        }
    }

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