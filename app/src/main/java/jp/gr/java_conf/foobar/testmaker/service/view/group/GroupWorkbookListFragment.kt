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
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.core.item.ClickableListItem
import com.example.ui.core.item.ConfirmActionListItem
import com.example.ui.group.GroupWorkbookListViewModel
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class GroupWorkbookListFragment : Fragment() {

    private val args: GroupWorkbookListFragmentArgs by navArgs()

    private val groupWorkbookListViewModel: GroupWorkbookListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var analytics: FirebaseAnalytics

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
                            val workbook = uiState.selectedSharedWorkbook?.workbook
                            val isOwner = uiState.selectedSharedWorkbook?.isOwner

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
                                            imageVector = Icons.Filled.History,
                                            contentDescription = "history"
                                        )
                                    },
                                    text = stringResource(id = R.string.history)
                                ) {
                                    analytics.logEvent(
                                        LogEvent.GROUP_BUTTON_HISTORY_WORKBOOK.eventName
                                    ) {}
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
                                    analytics.logEvent(
                                        LogEvent.GROUP_BUTTON_SHARE_WORKBOOK.eventName
                                    ) {}
                                    scope.launch {
                                        groupWorkbookListViewModel.onShareWorkbookClicked(workbook = workbook)
                                        drawerState.close()
                                    }
                                }
                                if (isOwner == true) {
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
                                            analytics.logEvent(
                                                LogEvent.GROUP_BUTTON_DELETE_WORKBOOK.eventName
                                            ) {}
                                            scope.launch {
                                                groupWorkbookListViewModel.onDeleteWorkbookClicked(
                                                    workbook
                                                )
                                                requireContext().showToast(getString(R.string.msg_delete_workbook))
                                                drawerState.close()
                                            }
                                        }
                                    )
                                }
                                Button(
                                    enabled = !uiState.isDownloading,
                                    onClick = {
                                        analytics.logEvent(
                                            LogEvent.GROUP_BUTTON_DOWNLOAD_WORKBOOK.eventName
                                        ) {}
                                        groupWorkbookListViewModel.onDownloadWorkbookClicked(
                                            workbook = workbook
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .defaultMinSize(minHeight = 48.dp),
                                ) {
                                    if (uiState.isDownloading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colors.onPrimary
                                        )
                                    } else {
                                        Text(text = stringResource(id = com.example.ui.R.string.download))
                                    }
                                }
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
                                                        requireActivity().hideKeyboard(windowToken)
                                                        findNavController().popBackStack()
                                                    }
                                            )
                                        },
                                        actions = {
                                            IconButton(
                                                onClick = {
                                                    analytics.logEvent(
                                                        LogEvent.GROUP_BUTTON_INVITE_GROUP.eventName
                                                    ) {}
                                                    groupWorkbookListViewModel.onInviteButtonClicked()
                                                },
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
                                                        confirmMessage = stringResource(
                                                            id = R.string.msg_exit_group,
                                                            uiState.group.getOrNull()?.name ?: ""
                                                        ),
                                                        confirmButtonText = stringResource(id = R.string.delete),
                                                        onConfirmed = {
                                                            analytics.logEvent(
                                                                LogEvent.GROUP_BUTTON_EXIT_GROUP.eventName
                                                            ) {}
                                                            groupWorkbookListViewModel.onExitGroupButtonClicked()
                                                            requireContext().showToast(getString(R.string.msg_success_exit_group))
                                                        }
                                                    )
                                                }
                                            }
                                        },
                                        title = stringResource(id = R.string.group_detail_fragment_label)
                                    )
                                },
                            ) { padding ->
                                Scaffold(
                                    content = { p ->
                                        RequireAuthentication(
                                            modifier = Modifier.padding(p),
                                            isLogin = uiState.isLogin,
                                            message = stringResource(id = R.string.msg_not_login_in_group),
                                            onLogin = groupWorkbookListViewModel::onUserCreated
                                        ) {
                                            SwipeRefresh(
                                                state = rememberSwipeRefreshState(
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
                                                                                    tint = ColorMapper(
                                                                                        LocalContext.current
                                                                                    ).colorToGraphicColor(
                                                                                        it.color
                                                                                    ),
                                                                                    contentDescription = "workbook",
                                                                                )
                                                                            },
                                                                            text = it.name,
                                                                            secondaryText = stringResource(
                                                                                id = R.string.num_questions,
                                                                                it.questionListCount
                                                                            ),
                                                                            onClick = {
                                                                                analytics.logEvent(
                                                                                    LogEvent.GROUP_ITEM_OPERATE_WORKBOOK.eventName
                                                                                ) {}
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

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.GROUP_DETAILS_SCREEN_OPEN.eventName)
        }
    }
}