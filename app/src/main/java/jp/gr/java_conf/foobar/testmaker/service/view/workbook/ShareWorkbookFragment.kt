package jp.gr.java_conf.foobar.testmaker.service.view.workbook

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


@AndroidEntryPoint
class ShareWorkbookFragment : Fragment() {

    private val testViewModel: TestViewModel by viewModels()
    private val uploadWorkbookViewModel: UploadWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

//            setContent {
//
//                var comment by remember { mutableStateOf("") }
//                var isPrivate by remember { mutableStateOf(false) }
//
//                TestMakerAndroidTheme {
//                    Scaffold(
//                        topBar = {
//                            TopAppBar(
//                                title = {
//                                    Text(
//                                        text = getString(R.string.title_upload_workbook),
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
//                                            Column(
//                                                modifier = Modifier
//                                                    .weight(weight = 1f, fill = true)
//                                            ) {
//
//                                                OutlinedTextField(
//                                                    modifier = Modifier
//                                                        .fillMaxWidth()
//                                                        .padding(bottom = 8.dp),
//                                                    enabled = false,
//                                                    label = {
//                                                        Text(text = stringResource(R.string.hint_share_workbook))
//                                                    },
//                                                    value = workbook.title,
//                                                    onValueChange = {})
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
//                                                OutlinedSwitch(
//                                                    label = stringResource(id = R.string.check_private),
//                                                    checked = isPrivate,
//                                                    onCheckedChange = {
//                                                        isPrivate = it
//                                                    })
//                                            }
//                                            Button(
//                                                onClick = {
//                                                    uploadWorkbookViewModel.uploadWorkbook(
//                                                        workbook = workbook,
//                                                        comment = comment,
//                                                        isPrivate = isPrivate
//                                                    )
//                                                },
//                                                modifier = Modifier
//                                                    .fillMaxWidth()
//                                                    .height(48.dp)
//                                            ) {
//                                                Text(text = stringResource(id = R.string.button_share_workbook))
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
//                                            uploadWorkbookViewModel.createDynamicLinks(state.documentId)
//                                        }
//                                        is LegacyUploadWorkbookUiState.UploadDivided -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_test_upload_divided))
//                                            findNavController().popBackStack()
//                                        }
//                                        is LegacyUploadWorkbookUiState.UploadFailure -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_upload_test_failure))
//                                            uploadWorkbookViewModel.reEdit()
//                                        }
//                                        is LegacyUploadWorkbookUiState.CreateDynamicLinksSuccess -> {
//                                            val sendIntent: Intent = Intent().apply {
//                                                action = Intent.ACTION_SEND
//                                                putExtra(
//                                                    Intent.EXTRA_TEXT,
//                                                    getString(R.string.msg_share_test, workbook.title, state.uri)
//                                                )
//                                                type = "text/plain"
//                                            }
//                                            val shareIntent = Intent.createChooser(sendIntent, null)
//                                            startActivity(shareIntent)
//                                            findNavController().popBackStack()
//                                        }
//                                        is LegacyUploadWorkbookUiState.CreateDynamicLinksFailure -> {
//                                            requireContext().showToast(requireContext().getString(R.string.msg_failure_share_test))
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
//            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adViewModel.setup()
    }
}