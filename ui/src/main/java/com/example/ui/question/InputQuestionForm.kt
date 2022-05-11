package com.example.ui.question

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.example.core.Constants
import com.example.core.utils.replaced
import com.example.ui.R
import com.example.ui.core.NumberPicker
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.OutlinedSwitch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun InputQuestionForm(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester,
    viewModel: FormQuestionViewModel,
    fragmentManager: FragmentManager
) {
    val focusManager = LocalFocusManager.current
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
    ) {
        item {
            Text(
                text = stringResource(id = R.string.header_required),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .padding(bottom = 8.dp),
                value = uiState.problem,
                label = {
                    Text(text = stringResource(R.string.hint_question))
                },
                onValueChange = viewModel::onProblemChanged
            )
        }

        itemsIndexed(items = uiState.answerList) { index, it ->
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = it,
                label = {
                    Text(text = stringResource(R.string.hint_answer))
                },
                onValueChange = {
                    viewModel.onAnswerListChanged(uiState.answerList.replaced(index, it))
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                })
            )
        }

        if (uiState.shouldShowOtherSelectionList) {

            itemsIndexed(items = uiState.otherSelectionList) { index, it ->

                if (uiState.isAutoGenerateOtherSelections) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        enabled = false,
                        value = stringResource(R.string.hint_auto),
                        onValueChange = {}
                    )
                } else {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        value = it,
                        label = {
                            Text(text = stringResource(R.string.hint_other))
                        },
                        onValueChange = {
                            viewModel.onOtherSelectionListChanged(
                                uiState.otherSelectionList.replaced(
                                    index,
                                    it
                                )
                            )
                        }
                    )
                }
            }
        }

        if (uiState.shouldShowAnswerListCount) {
            item {
                NumberPicker(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    min = 1,
                    max = Constants.ANSWER_INPUT_FORM_SIZE,
                    label = stringResource(id = R.string.picker_answers_size),
                    value = uiState.answerList.size,
                    onValueChange = {
                        viewModel.onAnswerListChanged(
                            List(it) { index ->
                                if (index < uiState.answerList.size) {
                                    uiState.answerList[index]
                                } else {
                                    ""
                                }
                            }
                        )
                    })
            }
        }

        if (uiState.shouldShowOtherSelectionListCount) {
            item {
                NumberPicker(
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                    min = 1,
                    max = Constants.OTHER_SELECTION_INPUT_FORM_SIZE,
                    label = stringResource(id = R.string.picker_wrong_size),
                    value = uiState.otherSelectionList.size,
                    onValueChange = {
                        viewModel.onOtherSelectionListChanged(
                            List(it) { index ->
                                if (index < uiState.otherSelectionList.size) {
                                    uiState.otherSelectionList[index]
                                } else {
                                    ""
                                }
                            }
                        )
                    })
            }
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
        item {
            Text(
                text = stringResource(id = R.string.header_optional),
                modifier = Modifier.padding(bottom = 4.dp),
                style = MaterialTheme.typography.caption
            )
        }
        item {
            ContentEditImageQuestion(
                image = uiState.problemImage,
                fragmentManager = fragmentManager,
                onValueChange = viewModel::onProblemImageUrlChanged
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                value = uiState.explanation,
                label = {
                    Text(text = stringResource(R.string.hint_explanation))
                },
                onValueChange = viewModel::onExplanationChanged
            )
        }
        if (uiState.shouldShowIsCheckAnswerOrder) {
            item {
                OutlinedSwitch(
                    modifier = Modifier.padding(bottom = 8.dp),
                    label = stringResource(id = R.string.switch_is_check_order),
                    checked = uiState.isCheckAnswerOrder,
                    onCheckedChange = viewModel::onIsCheckAnswerOrderChanged
                )
            }
        }
        if (uiState.shouldShowIsAutoGenerateOtherSelections) {
            item {
                OutlinedSwitch(
                    modifier = Modifier.padding(bottom = 8.dp),
                    label = stringResource(id = R.string.switch_is_auto_generate_others),
                    checked = uiState.isAutoGenerateOtherSelections,
                    onCheckedChange = viewModel::onIsAutoGenerateOtherSelectionsChanged
                )
            }
        }
    }
}