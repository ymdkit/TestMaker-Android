package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.core.TestMakerColor
import com.example.ui.core.*
import com.example.ui.logger.LogEvent

import com.example.ui.theme.TestMakerAndroidTheme
import com.example.ui.workbook.CreateWorkbookViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.util.TestMakerFileReader
import jp.gr.java_conf.foobar.testmaker.service.utils.hideKeyboard
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPicker
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.TextPicker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.job
import javax.inject.Inject


@AndroidEntryPoint
class CreateWorkbookFragment : Fragment() {

    private val args: CreateWorkbookFragmentArgs by navArgs()
    private val createWorkbookViewModel: CreateWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var colorMapper: ColorMapper

    @Inject
    lateinit var analytics: FirebaseAnalytics

    private val importFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it ?: return@registerForActivityResult
        val (title, content) = TestMakerFileReader.readFileFromUri(it, requireActivity())
        analytics.logEvent(LogEvent.HOME_SUCCESS_IMPORT_WORKBOOK.eventName) {}
        createWorkbookViewModel.importWorkbook(
            workbookName = title,
            exportedWorkbook = content
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(
                FirebaseAnalytics.Param.SCREEN_NAME,
                LogEvent.CREATE_WORKBOOK_SCREEN_OPEN.eventName
            )
        }
        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.fragment_create_workbook),
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

                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            LaunchedEffect(Unit) {
                                coroutineContext.job.invokeOnCompletion {
                                    focusRequester.requestFocus()
                                }
                            }

                            var name by rememberSaveable { mutableStateOf("") }
                            var color by remember { mutableStateOf(TestMakerColor.BLUE) }
                            val uiState by createWorkbookViewModel.uiState.collectAsState()

                            var showingValidationError by rememberSaveable { mutableStateOf(false) }

                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(weight = 1f, fill = true)
                                ) {
                                    if (uiState.isImportingWorkbook) {
                                        item {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }
                                    } else {
                                        item {
                                            Text(
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                text = stringResource(id = R.string.section_create_workbook_by_app)
                                            )
                                        }
                                        item {
                                            OutlinedTextField(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .focusRequester(focusRequester)
                                                    .padding(bottom = 8.dp),
                                                value = name,
                                                label = {
                                                    Text(text = stringResource(R.string.hint_workbook_name))
                                                },
                                                onValueChange = {
                                                    name = it
                                                },
                                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                                keyboardActions = KeyboardActions(onDone = {
                                                    focusManager.clearFocus()
                                                })
                                            )
                                        }
                                        item {
                                            ColorPicker(
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                label = stringResource(id = R.string.picker_color),
                                                value = color,
                                                colorMapper = colorMapper,
                                                onValueChange = {
                                                    analytics.logEvent(LogEvent.HOME_BUTTON_SET_COLOR_TO_WORKBOOK.eventName) {}
                                                    color = it
                                                }
                                            )
                                        }
                                        item {
                                            TextPicker(
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                label = stringResource(id = R.string.picker_folder),
                                                entries = uiState.folderList
                                                    .map { it.name } + listOf(
                                                    stringResource(id = R.string.new_folder)
                                                ),
                                                value = uiState.folderName ?: "",
                                                onValueChange = {
                                                    if (it == getString(R.string.new_folder)) {
                                                        findNavController().navigate(
                                                            CreateWorkbookFragmentDirections.actionCreateWorkbookToCreateFolder()
                                                        )
                                                    } else {
                                                        analytics.logEvent(LogEvent.HOME_BUTTON_SET_FOLDER_TO_WORKBOOK.eventName) {}
                                                        createWorkbookViewModel.onFolderChanged(it)
                                                    }
                                                })
                                        }
                                        item {
                                            Spacer(modifier = Modifier.height(32.dp))
                                        }
                                        item {
                                            Text(
                                                modifier = Modifier.padding(bottom = 8.dp),
                                                text = stringResource(id = R.string.section_create_workbook_by_import),
                                            )
                                        }
                                        item {
                                            OutlinedButton(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .padding(bottom = 8.dp),
                                                border = BorderStroke(
                                                    ButtonDefaults.OutlinedBorderSize,
                                                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                                                ),
                                                onClick = {
                                                    analytics.logEvent(LogEvent.HOME_BUTTON_IMPORT_WORKBOOK.eventName) {}
                                                    importFile.launch(arrayOf("text/*"))
                                                }) {
                                                Text(
                                                    text = stringResource(id = R.string.action_import),
                                                    color = MaterialTheme.colors.onBackground
                                                )
                                            }
                                        }
                                        item {
                                            OutlinedButton(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(56.dp)
                                                    .padding(bottom = 8.dp),
                                                border = BorderStroke(
                                                    ButtonDefaults.OutlinedBorderSize,
                                                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                                                ),
                                                onClick = {
                                                    analytics.logEvent(LogEvent.HOME_BUTTON_HELP_FOR_IMPORT_WORKBOOK.eventName) {}
                                                    startActivity(Intent(Intent.ACTION_VIEW).apply {
                                                        data =
                                                            Uri.parse("https://ankimaker.com/howto/edit_csv")
                                                    })
                                                }) {
                                                Text(
                                                    text = stringResource(id = R.string.help_import),
                                                    color = MaterialTheme.colors.onBackground
                                                )
                                            }
                                        }
                                    }
                                }
                                Button(
                                    onClick = {
                                        if (name.isEmpty()) {
                                            showingValidationError = true
                                            return@Button
                                        }

                                        createWorkbookViewModel.createWorkbook(
                                            name = name,
                                            color = color,
                                            folderName = uiState.folderName ?: "",
                                        )

                                        analytics.logEvent(LogEvent.HOME_BUTTON_STORE_WORKBOOK.eventName) {}
                                        requireContext().showToast(getString(R.string.msg_create_success_workbook))
                                        requireActivity().hideKeyboard(windowToken)
                                        findNavController().popBackStack()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                ) {
                                    Text(text = stringResource(id = R.string.button_create_workbook))
                                }
                                if (showingValidationError) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            showingValidationError = false
                                        },
                                        title = {
                                            Text(stringResource(id = R.string.title_error_create_workbook))
                                        },
                                        text = {
                                            Text(stringResource(id = R.string.msg_error_create_workbook))
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                showingValidationError = false
                                            }) {
                                                Text(stringResource(id = R.string.ok))
                                            }
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                AdView(viewModel = adViewModel)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createWorkbookViewModel.setup(folderName = args.folderName)
        adViewModel.setup()
        createWorkbookViewModel.load()

        lifecycleScope.launchWhenCreated {

            createWorkbookViewModel.importWorkbookCompletionEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.message_success_load, it))
                    findNavController().popBackStack()
                }
                .launchIn(this)
        }
    }

    override fun onResume() {
        super.onResume()
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(
                FirebaseAnalytics.Param.SCREEN_NAME,
                LogEvent.CREATE_WORKBOOK_SCREEN_OPEN.eventName
            )
        }
    }

}