package jp.gr.java_conf.foobar.testmaker.service.view.group

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
import androidx.navigation.fragment.navArgs
import com.example.ui.core.*
import com.example.ui.logger.LogEvent
import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.UploadWorkbookViewModel
import com.google.android.gms.ads.AdSize
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class UploadGroupTestFragment : Fragment() {

    private val args: UploadGroupTestFragmentArgs by navArgs()

    private val uploadWorkbookViewModel: UploadWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var analytics: FirebaseAnalytics

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
                                                requireActivity().hideKeyboard(windowToken)
                                                findNavController().popBackStack()
                                            }
                                    )
                                }
                            )
                        },
                        content = { padding ->
                            Column {
                                Scaffold(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(weight = 1f, fill = true)
                                ) { padding ->
                                    RequireAuthentication(
                                        modifier = Modifier.padding(padding),
                                        isLogin = uiState.isLogin,
                                        message = stringResource(id = R.string.msg_not_login_in_group),
                                        onLogin = uploadWorkbookViewModel::onUserCreated
                                    ) {

                                        ResourceContent(
                                            resource = uiState.workbookList,
                                            onRetry = { uploadWorkbookViewModel.load() }) {
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
                                                                    text = uiState.selectedWorkbook?.name
                                                                        ?: stringResource(R.string.msg_empty_uploadable_workbook),
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
                                                                it.forEach {
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
                                                }
                                                Button(
                                                    enabled = !uiState.isUploading,
                                                    onClick = {
                                                        analytics.logEvent(
                                                            LogEvent.QUESTIONS_BUTTON_MOVE_QUESTIONS.eventName
                                                        ) {}
                                                        uploadWorkbookViewModel.uploadWorkbook()
                                                    },
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
        uploadWorkbookViewModel.setup(args.groupId, true)

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