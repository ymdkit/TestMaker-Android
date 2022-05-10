package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.core.AdView
import com.example.ui.core.AdViewModel
import com.example.ui.core.showToast
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.android.gms.ads.AdSize
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.OutlinedSwitch
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.RequestLogin


@AndroidEntryPoint
class ShareWorkbookFragment : Fragment() {

    private val testViewModel: TestViewModel by viewModels()
    private val uploadWorkbookViewModel: UploadWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    private val args: UploadWorkbookFragmentArgs by navArgs()

    private val workbook: Test by lazy { testViewModel.get(args.workbookId) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {

            setContent {

                var comment by remember { mutableStateOf("") }
                var isPrivate by remember { mutableStateOf(false) }

                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.title_upload_workbook),
                                    )
                                },
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
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .weight(weight = 1f, fill = true)
                                ) {

                                    val uiState = uploadWorkbookViewModel.uiState.collectAsState()

                                    when (val state = uiState.value) {
                                        is UploadWorkbookUiState.UnAuthorized -> {
                                            RequestLogin(
                                                onSuccess = {
                                                    requireContext().showToast(
                                                        requireContext().getString(
                                                            R.string.msg_success_login
                                                        )
                                                    )
                                                    uploadWorkbookViewModel.setUser(it)
                                                },
                                                onFailure = {
                                                    requireContext().showToast(
                                                        requireContext().getString(
                                                            R.string.msg_failure_login,
                                                            it ?: -1
                                                        )
                                                    )
                                                })
                                        }
                                        is UploadWorkbookUiState.Editing -> {
                                            Column(
                                                modifier = Modifier
                                                    .weight(weight = 1f, fill = true)
                                            ) {

                                                OutlinedTextField(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 8.dp),
                                                    enabled = false,
                                                    label = {
                                                        Text(text = stringResource(R.string.hint_share_workbook))
                                                    },
                                                    value = workbook.title,
                                                    onValueChange = {})
                                                OutlinedTextField(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(bottom = 16.dp),
                                                    maxLines = 3,
                                                    label = {
                                                        Text(text = stringResource(R.string.hint_overview))
                                                    },
                                                    value = comment,
                                                    onValueChange = {
                                                        comment = it
                                                    })
                                                OutlinedSwitch(
                                                    label = stringResource(id = R.string.check_private),
                                                    checked = isPrivate,
                                                    onCheckedChange = {
                                                        isPrivate = it
                                                    })
                                            }
                                            Button(
                                                onClick = {
                                                    uploadWorkbookViewModel.uploadWorkbook(
                                                        workbook = workbook,
                                                        comment = comment,
                                                        isPrivate = isPrivate
                                                    )
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                            ) {
                                                Text(text = stringResource(id = R.string.button_share_workbook))
                                            }
                                        }
                                        is UploadWorkbookUiState.Loading -> {
                                            Column(
                                                modifier = Modifier.fillMaxSize(),
                                                verticalArrangement = Arrangement.Center,
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                        is UploadWorkbookUiState.UploadSuccess -> {
                                            uploadWorkbookViewModel.createDynamicLinks(state.documentId)
                                        }
                                        is UploadWorkbookUiState.UploadDivided -> {
                                            requireContext().showToast(requireContext().getString(R.string.msg_test_upload_divided))
                                            findNavController().popBackStack()
                                        }
                                        is UploadWorkbookUiState.UploadFailure -> {
                                            requireContext().showToast(requireContext().getString(R.string.msg_upload_test_failure))
                                            uploadWorkbookViewModel.reEdit()
                                        }
                                        is UploadWorkbookUiState.CreateDynamicLinksSuccess -> {
                                            val sendIntent: Intent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    getString(R.string.msg_share_test, workbook.title, state.uri)
                                                )
                                                type = "text/plain"
                                            }
                                            val shareIntent = Intent.createChooser(sendIntent, null)
                                            startActivity(shareIntent)
                                            findNavController().popBackStack()
                                        }
                                        is UploadWorkbookUiState.CreateDynamicLinksFailure -> {
                                            requireContext().showToast(requireContext().getString(R.string.msg_failure_share_test))
                                            uploadWorkbookViewModel.reEdit()
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
    }
}