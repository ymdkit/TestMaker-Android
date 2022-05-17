package jp.gr.java_conf.foobar.testmaker.service.view.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.core.TestMakerColor
import com.example.ui.core.*
import com.example.ui.folder.CreateFolderViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPicker
import kotlinx.coroutines.job
import javax.inject.Inject

@AndroidEntryPoint
class CreateFolderFragment : Fragment() {

    private val createFolderViewModel: CreateFolderViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var colorMapper: ColorMapper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.fragment_create_folder)
                            )
                        },
                        content = {

                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            LaunchedEffect(Unit) {
                                coroutineContext.job.invokeOnCompletion {
                                    focusRequester.requestFocus()
                                }
                            }

                            var name by rememberSaveable { mutableStateOf("") }
                            var color by remember { mutableStateOf(TestMakerColor.BLUE) }

                            var showingValidationError by rememberSaveable { mutableStateOf(false) }

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
                                                Text(text = stringResource(R.string.hint_category_name))
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
                                            value = color,
                                            colorMapper = colorMapper,
                                            onValueChange = {
                                                color = it
                                            }
                                        )
                                    }
                                    Button(
                                        onClick = {

                                            if (name.isEmpty()) {
                                                showingValidationError = true
                                                return@Button
                                            }

                                            createFolderViewModel.createFolder(
                                                name = name,
                                                color = color
                                            )
                                            requireContext().showToast(getString(R.string.msg_create_folder))
                                            findNavController().popBackStack()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                    ) {
                                        Text(text = stringResource(id = R.string.button_create_folder))
                                    }
                                    if (showingValidationError) {
                                        AlertDialog(
                                            onDismissRequest = {
                                                showingValidationError = false
                                            },
                                            title = {
                                                Text(stringResource(id = R.string.title_error_create_folder))
                                            },
                                            text = {
                                                Text(stringResource(id = R.string.msg_error_create_folder))
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
                                AdView(adViewModel)
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