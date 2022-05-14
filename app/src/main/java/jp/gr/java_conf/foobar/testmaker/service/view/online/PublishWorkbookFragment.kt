package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.ui.core.AdViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.workbook.UploadWorkbookViewModel

@AndroidEntryPoint
class PublishWorkbookFragment : Fragment() {

    private val testViewModel: TestViewModel by viewModels()
    private val uploadWorkbookViewModel: UploadWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {
//                TestMakerAndroidTheme {
//                    Scaffold(
//                        topBar = {
//                            TopAppBar(
//                                title = {
//                                    Text(
//                                        text = getString(R.string.title_publish_workbook),
//                                    )
//                                },
//                                navigationIcon = {
//                                    Icon(
//                                        imageVector = Icons.Filled.ArrowBack,
//                                        contentDescription = "Back",
//                                        modifier = Modifier
//                                            .padding(16.dp)
//                                            .clickable {
//                                                findNavController().popBackStack()
//                                            }
//                                    )
//                                }
//                            )
//                        },
//                        content = {
//
//                            Column {
//                                Column(
//                                    modifier = Modifier
//                                        .padding(16.dp)
//                                        .weight(weight = 1f, fill = true)
//                                ) {
//
//                                    val uiState = uploadWorkbookViewModel.uiState.collectAsState()
//
//                                    when (val state = uiState.value) {
//                                        is LegacyUploadWorkbookUiState.UnAuthorized -> {
//                                            RequestLogin(
//                                                onSuccess = {
//                                                    requireContext().showToast(
//                                                        requireContext().getString(
//                                                            R.string.msg_success_login
//                                                        )
//                                                    )
//                                                    uploadWorkbookViewModel.setUser(it)
//                                                },
//                                                onFailure = {
//                                                    requireContext().showToast(
//                                                        requireContext().getString(
//                                                            R.string.msg_failure_login,
//                                                            it ?: -1
//                                                        )
//                                                    )
//                                                })
//                                        }
//                                        is LegacyUploadWorkbookUiState.Editing -> {
//
//                                            var workbook by remember { mutableStateOf(workbook) }
//                                            var comment by rememberSaveable { mutableStateOf("") }
//                                            var showingDropDownMenu by remember {
//                                                mutableStateOf(
//                                                    false
//                                                )
//                                            }
//
//                                            Column(
//                                                modifier = Modifier
//                                                    .weight(weight = 1f, fill = true)
//                                            ) {
//
//                                                Box {
//                                                    OutlinedButton(
//                                                        modifier = Modifier
//                                                            .fillMaxWidth()
//                                                            .height(56.dp),
//                                                        onClick = {
//                                                            showingDropDownMenu = true
//                                                        },
//                                                        border = BorderStroke(
//                                                            ButtonDefaults.OutlinedBorderSize,
//                                                            MaterialTheme.colors.onSurface.copy(
//                                                                alpha = ContentAlpha.disabled
//                                                            )
//                                                        )
//                                                    ) {
//                                                        Text(
//                                                            text = stringResource(id = R.string.label_workbook),
//                                                            color = MaterialTheme.colors.onSurface
//                                                        )
//                                                        Spacer(
//                                                            modifier = Modifier.weight(
//                                                                weight = 1f,
//                                                                fill = true
//                                                            )
//                                                        )
//                                                        Text(
//                                                            text = workbook.title,
//                                                            color = MaterialTheme.colors.onSurface
//                                                        )
//                                                        Icon(
//                                                            Icons.Default.ArrowDropDown,
//                                                            contentDescription = null,
//                                                            tint = MaterialTheme.colors.onSurface.copy(
//                                                                alpha = ContentAlpha.disabled
//                                                            )
//                                                        )
//                                                    }
//
//                                                    DropdownMenu(
//                                                        expanded = showingDropDownMenu,
//                                                        onDismissRequest = {
//                                                            showingDropDownMenu = false
//                                                        }) {
//                                                        testViewModel.tests.forEach {
//                                                            DropdownMenuItem(onClick = {
//                                                                workbook = it
//                                                                showingDropDownMenu = false
//                                                            }) {
//                                                                Text(it.title)
//                                                            }
//                                                        }
//                                                    }
//                                                }
//
//                                                OutlinedTextField(
//                                                    modifier = Modifier
//                                                        .fillMaxWidth()
//                                                        .padding(bottom = 16.dp),
//                                                    maxLines = 3,
//                                                    label = {
//                                                        Text(text = stringResource(R.string.hint_overview))
//                                                    },
//                                                    value = comment,
//                                                    onValueChange = {
//                                                        comment = it
//                                                    })
//                                            }
//                                            Button(
//                                                onClick = {
//                                                    uploadWorkbookViewModel.uploadWorkbook(
//                                                        workbook = workbook,
//                                                        comment = comment,
//                                                        isPrivate = false
//                                                    )
//                                                },
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .height(48.dp)
//                                            ) {
//                                                Text(text = stringResource(id = R.string.button_publish_workbook))
//                                            }
//                                        }
//                                        is LegacyUploadWorkbookUiState.Loading -> {
//                                            Column(
//                                                modifier = Modifier.fillMaxSize(),
//                                                verticalArrangement = Arrangement.Center,
//                                                horizontalAlignment = Alignment.CenterHorizontally,
//                                            ) {
//                                                CircularProgressIndicator()
//                                            }
//                                        }
//                                        is LegacyUploadWorkbookUiState.UploadSuccess -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_test_upload))
//                                            findNavController().popBackStack()
//                                        }
//                                        is LegacyUploadWorkbookUiState.UploadDivided -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_test_upload_divided))
//                                            findNavController().popBackStack()
//                                        }
//                                        is LegacyUploadWorkbookUiState.UploadFailure -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_upload_test_failure))
//                                            uploadWorkbookViewModel.reEdit()
//                                        }
//                                    }
//                                }
//                                AdView(
//                                    viewModel = adViewModel,
//                                    adSize = AdSize.MEDIUM_RECTANGLE
//                                )
//                            }
//                        }
//                    )
//                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adViewModel.setup()
    }
}