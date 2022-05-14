package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.ui.core.*
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.usecase.utils.Resource
import com.google.android.gms.ads.AdSize
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.OutlinedSwitch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow


@AndroidEntryPoint
class UploadWorkbookFragment : Fragment() {

    private val uploadWorkbookViewModel: UploadWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
                val uiState by uploadWorkbookViewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.title_activity_upload_test),
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
                                }
                            )
                        },
                        content = {
                            Column {
                                Scaffold(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(weight = 1f, fill = true)
                                ) {
                                    RequireAuthentication(
                                        isLogin = uiState.isLogin,
                                        onLogin = uploadWorkbookViewModel::onUserCreated
                                    ) {

                                        when (val state = uiState.workbookList) {
                                            is Resource.Success -> {
                                                Column {
                                                    LazyColumn(
                                                        modifier = Modifier
                                                            .weight(weight = 1f, fill = true)
                                                    ) {
                                                        item {
                                                            Box {
                                                                OutlinedButton(
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .height(56.dp),
                                                                    onClick = uploadWorkbookViewModel::onToggleDropDownMenu,
                                                                    border = BorderStroke(
                                                                        ButtonDefaults.OutlinedBorderSize,
                                                                        MaterialTheme.colors.onSurface.copy(
                                                                            alpha = ContentAlpha.disabled
                                                                        )
                                                                    )
                                                                ) {
                                                                    Text(
                                                                        text = stringResource(id = R.string.label_workbook),
                                                                        color = MaterialTheme.colors.onSurface
                                                                    )
                                                                    Spacer(
                                                                        modifier = Modifier.weight(
                                                                            weight = 1f,
                                                                            fill = true
                                                                        )
                                                                    )
                                                                    Text(
                                                                        // todo StringResource
                                                                        text = uiState.selectedWorkbook?.name
                                                                            ?: "投稿可能な問題集が存在しません",
                                                                        color = MaterialTheme.colors.onSurface
                                                                    )
                                                                    Icon(
                                                                        Icons.Default.ArrowDropDown,
                                                                        contentDescription = null,
                                                                        tint = MaterialTheme.colors.onSurface.copy(
                                                                            alpha = ContentAlpha.disabled
                                                                        )
                                                                    )
                                                                }
                                                                DropdownMenu(
                                                                    expanded = uiState.showingDropDownMenu,
                                                                    onDismissRequest = uploadWorkbookViewModel::onToggleDropDownMenu
                                                                ) {
                                                                    state.value.forEach {
                                                                        DropdownMenuItem(onClick = {
                                                                            uploadWorkbookViewModel.onWorkbookSelected(
                                                                                it
                                                                            )
                                                                            uploadWorkbookViewModel.onToggleDropDownMenu()
                                                                        }) {
                                                                            Text(it.name)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        item {
                                                            OutlinedTextField(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .padding(bottom = 8.dp),
                                                                maxLines = 3,
                                                                label = {
                                                                    Text(text = stringResource(R.string.hint_overview))
                                                                },
                                                                value = uiState.comment,
                                                                onValueChange = uploadWorkbookViewModel::onCommentChanged
                                                            )
                                                        }
                                                        item {
                                                            OutlinedSwitch(
                                                                label = stringResource(id = R.string.check_private),
                                                                checked = uiState.isPrivateUpload,
                                                                onCheckedChange = uploadWorkbookViewModel::onIsPrivateUploadChanged
                                                            )
                                                        }
                                                    }
                                                    Button(
                                                        enabled = !uiState.isUploading,
                                                        onClick = uploadWorkbookViewModel::uploadWorkbook,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(48.dp)
                                                    ) {
                                                        if (uiState.isUploading) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(32.dp),
                                                                color = MaterialTheme.colors.onPrimary
                                                            )
                                                        } else {
                                                            Text(text = stringResource(id = R.string.button_upload_workbook))
                                                        }
                                                    }
                                                }
                                            }
                                            else -> {
                                                // todo
                                            }
                                        }
                                    }
                                }
                                AdView(
                                    viewModel = adViewModel,
                                    adSize = AdSize.MEDIUM_RECTANGLE
                                )
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
        uploadWorkbookViewModel.setup("", false)

        lifecycleScope.launchWhenCreated {
            uploadWorkbookViewModel.uploadWorkbookEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(
                        requireContext().getString(
                            R.string.msg_test_upload
                        )
                    )
                    findNavController().popBackStack()
                }
                .launchIn(this)
        }
    }
}