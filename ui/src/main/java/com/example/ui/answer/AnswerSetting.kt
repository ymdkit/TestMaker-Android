package com.example.ui.answer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.QuestionCondition
import com.example.ui.R
import com.example.ui.core.ContainedWideButton
import com.example.ui.core.item.CheckboxListItem
import com.example.ui.core.item.EditTextListItem

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AnswerSetting(
    workbookName: String,
    onStartButtonClicked: () -> Unit,
    answerSettingViewModel: AnswerSettingViewModel
) {
    val uiState by answerSettingViewModel.uiState.collectAsState()

    ListItem(
        text = {
            Text(
                text = workbookName,
            )
        }
    )
    CheckboxListItem(
        label = stringResource(id = R.string.is_random),
        checked = uiState.isRandomOrder,
        onCheckedChanged = answerSettingViewModel::onIsRandomOrderChanged
    )
    CheckboxListItem(
        label = stringResource(id = R.string.message_wrong_only),
        checked = uiState.questionCondition == QuestionCondition.WRONG,
        onCheckedChanged = answerSettingViewModel::onQuestionConditionChanged
    )
    CheckboxListItem(
        label = stringResource(id = R.string.message_self),
        checked = uiState.isSelfScoring,
        onCheckedChanged = answerSettingViewModel::onIsSelfScoringChanged
    )
    CheckboxListItem(
        label = stringResource(id = R.string.always_review),
        checked = uiState.isAlwaysShowExplanation,
        onCheckedChanged = answerSettingViewModel::onIsAlwaysShowExplanationChanged
    )
    CheckboxListItem(
        label = stringResource(id = R.string.setting_show_dialog),
        checked = uiState.isShowAnswerSettingDialog,
        onCheckedChanged = answerSettingViewModel::onIsShowAnswerSettingDialogChanged
    )
    EditTextListItem(
        label = stringResource(id = R.string.position_start),
        value = (uiState.startPosition + 1).toString(),
        keyboardType = KeyboardType.Number,
        dialogTitle = stringResource(id = R.string.position_start),
        onValueSubmitted = {
            answerSettingViewModel.onStartPositionChanged(it.toInt() - 1)
        },
    )
    EditTextListItem(
        label = stringResource(id = R.string.number_questions),
        value = uiState.questionCount.toString(),
        keyboardType = KeyboardType.Number,
        dialogTitle = stringResource(id = R.string.number_questions),
        onValueSubmitted = {
            answerSettingViewModel.onQuestionCountChanged(it.toInt())
        },
    )
    ContainedWideButton(
        modifier = Modifier.padding(16.dp),
        onClick = onStartButtonClicked,
        text = stringResource(id = R.string.start)
    )
}