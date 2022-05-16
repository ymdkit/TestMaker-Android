package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.ui.core.*
import com.example.ui.group.GroupListItem
import com.example.ui.group.GroupListViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val groupListViewModel: GroupListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

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
                        ) {
                            RequireAuthentication(
                                isLogin = uiState.isLogin,
                                message = stringResource(id = R.string.msg_not_login_in_group),
                                content = {
                                    Scaffold(
                                        content = {
                                            SwipeRefresh(
                                                state = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing),
                                                onRefresh = groupListViewModel::load
                                            ) {
                                                ResourceContent(
                                                    resource = uiState.groupList,
                                                    onRetry = groupListViewModel::load
                                                ) {
                                                    LazyColumn(
                                                        modifier = Modifier.fillMaxHeight()
                                                    ) {
                                                        it.forEach {
                                                            item {
                                                                GroupListItem(
                                                                    group = it,
                                                                    onClick = {
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
                                        },
                                        floatingActionButton = {
                                            FloatingActionButton(onClick = groupListViewModel::onCreateGroupButtonClicked) {
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
}