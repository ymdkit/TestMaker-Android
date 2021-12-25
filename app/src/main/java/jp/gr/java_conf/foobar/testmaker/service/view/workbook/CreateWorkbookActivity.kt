package jp.gr.java_conf.foobar.testmaker.service.view.workbook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
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

class CreateWorkbookActivity : AppCompatActivity() {


    companion object {

        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, CreateWorkbookActivity::class.java))
        }
    }

    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val testViewModel: TestViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private val logger: TestMakerLogger by inject()

    private val colors by lazy {
        listOf(
            ColorPickerItem(colorId = R.color.red, name = getString(R.string.red)),
            ColorPickerItem(colorId = R.color.orange, name = getString(R.string.orange)),
            ColorPickerItem(colorId = R.color.yellow, name = getString(R.string.yellow)),
            ColorPickerItem(colorId = R.color.green, name = getString(R.string.green)),
            ColorPickerItem(colorId = R.color.dark_green, name = getString(R.string.dark_green)),
            ColorPickerItem(colorId = R.color.blue, name = getString(R.string.blue)),
            ColorPickerItem(colorId = R.color.navy, name = getString(R.string.navy)),
            ColorPickerItem(colorId = R.color.purple, name = getString(R.string.purple)),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        )
                    },
                    content = {

                        val focusRequester = remember { FocusRequester() }
                        val focusManager = LocalFocusManager.current
                        LaunchedEffect(Unit) {
                            focusRequester.requestFocus()
                        }

                        var name by rememberSaveable { mutableStateOf("") }
                        var colorId by rememberSaveable { mutableStateOf(colors.first().colorId) }
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
                                        value = colorId,
                                        onValueChange = {
                                            colorId = it
                                        }
                                    )
                                    TextPicker(
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        label = stringResource(id = R.string.picker_folder),
                                        entries = categoryViewModel.getCategories().map { it.name },
                                        value = folderName,
                                        onValueChange = {
                                            folderName = it
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
                                            color = colorId,
                                            category = folderName,
                                            source = CreateTestSource.SELF.title
                                        )
                                        logger.logCreateTestEvent(name, CreateTestSource.SELF.title)

                                        finish()
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