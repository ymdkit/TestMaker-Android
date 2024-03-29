package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ui.core.*
import com.example.ui.group.GroupListItem
import com.example.ui.group.GroupListViewModel
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import javax.inject.Inject

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val groupListViewModel: GroupListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var analytics: FirebaseAnalytics

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by groupListViewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Column {
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            topBar = { TestMakerTopAppBar(title = stringResource(id = R.string.group_list_fragment_label)) },
                        ) { padding ->
                            RequireAuthentication(
                                isLogin = uiState.isLogin,
                                message = stringResource(id = R.string.msg_not_login_in_group),
                                content = {
                                    Scaffold(
                                        content = { p ->
                                            SwipeRefresh(
                                                modifier = Modifier.padding(p),
                                                state = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing),
                                                onRefresh = groupListViewModel::load
                                            ) {
                                                ResourceContent(
                                                    resource = uiState.groupList,
                                                    onRetry = groupListViewModel::load
                                                ) {
                                                    if (it.isEmpty()) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .padding(16.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(text = stringResource(id = R.string.empty_group))
                                                        }
                                                    } else {
                                                        LazyColumn(
                                                            modifier = Modifier.fillMaxHeight()
                                                        ) {
                                                            it.forEach {
                                                                item {
                                                                    GroupListItem(
                                                                        group = it,
                                                                        onClick = {
                                                                            analytics.logEvent(
                                                                                LogEvent.GROUP_ITEM_OPEN_GROUP.eventName
                                                                            ) {}
                                                                            findNavController().navigate(
                                                                                GroupListFragmentDirections.actionGroupListToGroupDetail(
                                                                                    groupId = it.id
                                                                                )
                                                                            )
                                                                        })
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
                                                    LogEvent.GROUP_BUTTON_CREATE_GROUP.eventName
                                                ) {}
                                                groupListViewModel.onCreateGroupButtonClicked()
                                            }) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    contentDescription = "create group"
                                                )
                                            }
                                        }
                                    )
                                },
                                onLogin = groupListViewModel::onUserCreated
                            )
                        }
                        AdView(viewModel = adViewModel)
                    }
                    if (uiState.showingCreateGroupDialog) {
                        EditTextDialog(
                            title = stringResource(id = R.string.title_create_group),
                            value = uiState.editingGroupName,
                            onValueChanged = groupListViewModel::onGroupNameChanged,
                            placeholder = stringResource(id = R.string.hint_group_name),
                            onDismiss = groupListViewModel::onCancelCreateGroupButtonClicked,
                            onSubmit = {
                                analytics.logEvent(
                                    LogEvent.GROUP_BUTTON_STORE_GROUP.eventName
                                ) {}
                                groupListViewModel.onCreateGroup(it)
                                requireContext().showToast(getString(R.string.msg_success_create_group))
                            },
                            validated = { it.isNotEmpty() }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupListViewModel.setup()
        groupListViewModel.load()
    }

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, LogEvent.GROUP_SCREEN_OPEN.eventName)
        }
    }
}