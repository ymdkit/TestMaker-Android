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
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import javax.inject.Inject

@AndroidEntryPoint
class GroupListFragment : Fragment() {

    private val groupListViewModel: GroupListViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                val uiState = groupListViewModel.uiState.collectAsState()

                // swipeRefresh の導入
                TestMakerAndroidTheme {
                    Column {
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            topBar = { TestMakerTopAppBar(title = stringResource(id = R.string.group_list_fragment_label)) },
                            content = {
                                // todo 非ログイン時の UI + ログイン機構
                                ResourceContent(
                                    resource = uiState.value.groupList,
                                    onSuccess = {
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
                                    },
                                    onRetry = groupListViewModel::load
                                )
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
                        AdView(viewModel = adViewModel)
                    }
                    if (uiState.value.showingCreateGroupDialog) {
                        EditTextDialog(
                            title = stringResource(id = R.string.title_create_group),
                            value = uiState.value.editingGroupName,
                            onValueChanged = groupListViewModel::onGroupNameChanged,
                            placeholder = stringResource(id = R.string.hint_group_name),
                            onDismiss = groupListViewModel::onCancelCreateGroupButtonClicked,
                            onSubmit = groupListViewModel::onCreateGroup
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