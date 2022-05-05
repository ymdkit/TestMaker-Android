package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.ui.core.AdViewModel
import com.example.ui.core.ComposeAdView
import com.example.ui.core.showToast
import com.example.ui.workbook.EditWorkbookViewModel
import com.example.usecase.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.edit.QuestionListFragmentArgs
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPicker
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPickerItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.TextPicker
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import javax.inject.Inject


@AndroidEntryPoint
class EditWorkbookFragment : Fragment() {

    private val editWorkbookViewModel: EditWorkbookViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var logger: TestMakerLogger

    private val colors by lazy {
        listOf(
            ColorPickerItem(id = 0, colorId = R.color.red, name = getString(R.string.red)),
            ColorPickerItem(id = 1, colorId = R.color.orange, name = getString(R.string.orange)),
            ColorPickerItem(id = 2, colorId = R.color.yellow, name = getString(R.string.yellow)),
            ColorPickerItem(id = 3, colorId = R.color.green, name = getString(R.string.green)),
            ColorPickerItem(
                id = 4,
                colorId = R.color.dark_green,
                name = getString(R.string.dark_green)
            ),
            ColorPickerItem(id = 5, colorId = R.color.blue, name = getString(R.string.blue)),
            ColorPickerItem(id = 6, colorId = R.color.navy, name = getString(R.string.navy)),
            ColorPickerItem(id = 7, colorId = R.color.purple, name = getString(R.string.purple)),
        )
    }

    private val args: QuestionListFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = getString(R.string.title_edit_workbook),
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

                            val uiState by editWorkbookViewModel.uiState.collectAsState()

                            when (val state = uiState) {
                                is Resource.Success -> {

                                    val workbook = state.value.workbook

                                    val focusRequester = remember { FocusRequester() }
                                    val focusManager = LocalFocusManager.current
                                    LaunchedEffect(Unit) {
                                        focusRequester.requestFocus()
                                    }

                                    var name by rememberSaveable { mutableStateOf(workbook.name) }
                                    var color by remember {
                                        mutableStateOf(colors.find {
                                            workbook.color == ContextCompat.getColor(
                                                context,
                                                it.colorId
                                            )
                                        } ?: colors.first())
                                    }
                                    var folderName by rememberSaveable { mutableStateOf(workbook.folderName) }

                                    var showingValidationError by rememberSaveable {
                                        mutableStateOf(
                                            false
                                        )
                                    }

                                    Column {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .weight(weight = 1f, fill = true)
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .weight(weight = 1f, fill = true)
                                            ) {
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
                                                ColorPicker(
                                                    modifier = Modifier.padding(bottom = 8.dp),
                                                    label = stringResource(id = R.string.picker_color),
                                                    entries = colors,
                                                    value = color,
                                                    onValueChange = {
                                                        color = it
                                                    }
                                                )
                                                TextPicker(
                                                    modifier = Modifier.padding(bottom = 8.dp),
                                                    label = stringResource(id = R.string.picker_folder),
                                                    entries = state.value.folderList
                                                        .map { it.name } + listOf(
                                                        stringResource(id = R.string.new_folder)
                                                    ),
                                                    value = folderName,
                                                    onValueChange = {
                                                        if (it == getString(R.string.new_folder)) {
                                                            findNavController().navigate(
                                                                EditWorkbookFragmentDirections.actionEditWorkbookToCreateFolder()
                                                            )
                                                        } else {
                                                            folderName = it
                                                        }
                                                    })

                                            }
                                            Button(
                                                onClick = {

                                                    if (name.isEmpty()) {
                                                        showingValidationError = true
                                                        return@Button
                                                    }

                                                    editWorkbookViewModel.updateWorkbook(
                                                        name = name,
                                                        color = ContextCompat.getColor(
                                                            context,
                                                            color.colorId
                                                        ),
                                                        folderName = folderName
                                                    )

                                                    requireContext().showToast(getString(R.string.msg_update_success_workbook))

                                                    findNavController().popBackStack()
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                            ) {
                                                Text(text = stringResource(id = R.string.button_update_workbook))
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
                                        }
                                        ComposeAdView(viewModel = adViewModel)
                                    }
                                }
                                else -> {
                                    // do nothing
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        editWorkbookViewModel.setup(workbookId = args.workbookId)
        editWorkbookViewModel.load()
    }
}