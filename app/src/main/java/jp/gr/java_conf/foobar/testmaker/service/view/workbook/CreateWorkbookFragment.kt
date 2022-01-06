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
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.navigation.findNavController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPicker
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ColorPickerItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.TextPicker
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateWorkbookFragment : Fragment() {

    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val testViewModel: TestViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private val logger: TestMakerLogger by inject()

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
                                        text = getString(R.string.title_activity_create_workbook),
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

                            val focusRequester = remember { FocusRequester() }
                            val focusManager = LocalFocusManager.current
                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }

                            var name by rememberSaveable { mutableStateOf("") }
                            var color by remember { mutableStateOf(colors.first()) }
                            val folders by categoryViewModel.categoriesLiveData.observeAsState(
                                listOf()
                            )
                            var folderName by rememberSaveable { mutableStateOf("") }

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
                                            entries = folders
                                                .map { it.name } + listOf(
                                                stringResource(id = R.string.new_folder)
                                            ),
                                            value = folderName,
                                            onValueChange = {
                                                if (it == getString(R.string.new_folder)) {
                                                    findNavController().navigate(CreateWorkbookFragmentDirections.actionCreateWorkbookToCreateFolder())
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

                                            testViewModel.create(
                                                title = name,
                                                color = ContextCompat.getColor(
                                                    context,
                                                    color.colorId
                                                ),
                                                category = folderName,
                                                source = CreateTestSource.SELF.title
                                            )
                                            logger.logCreateTestEvent(
                                                name,
                                                CreateTestSource.SELF.title
                                            )

                                            requireContext().showToast(getString(R.string.msg_create_success_workbook))

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
                                }
                                ComposeAdView(
                                    isRemovedAd = sharedPreferenceManager.isRemovedAd,
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}