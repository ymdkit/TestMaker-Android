package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.core.QuestionCondition
import com.example.ui.answer.AnswerSettingViewModel
import com.example.ui.core.ContainedWideButton
import com.example.ui.home.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireLongArgument
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme

@AndroidEntryPoint
class AnswerSettingDialogFragment : BottomSheetDialogFragment() {

    companion object {
        const val ARG_WORKBOOK_NAME = "workbookName"
        const val ARG_WORKBOOK_ID = "workbookId"

        fun newInstance(
            workbookId: Long,
            workbookName: String
        ): AnswerSettingDialogFragment =
            AnswerSettingDialogFragment().apply {
                arguments = bundleOf(
                    ARG_WORKBOOK_ID to workbookId,
                    ARG_WORKBOOK_NAME to workbookName,
                )
            }
    }

    private val answerSettingViewModel: AnswerSettingViewModel by viewModels()
    private val homeViewModel: HomeViewModel by activityViewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return ComposeView(requireContext()).apply {
            setContent {

                val uiState by answerSettingViewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Surface {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            ListItem(
                                text = {
                                    Text(
                                        text = requireStringArgument(ARG_WORKBOOK_NAME),
                                        fontWeight = FontWeight.Bold
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
                                label = stringResource(id = R.string.message_switch_question),
                                checked = uiState.isSwapProblemAndAnswer,
                                onCheckedChanged = answerSettingViewModel::onIsSwapProblemAndAnswerChanged
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
                                label = stringResource(id = R.string.setting_sound),
                                checked = uiState.isPlaySound,
                                onCheckedChanged = answerSettingViewModel::onIsPlaySoundChanged
                            )
                            CheckboxListItem(
                                label = stringResource(id = R.string.setting_show_dialog),
                                checked = uiState.isShowAnswerSettingDialog,
                                onCheckedChanged = answerSettingViewModel::onIsShowAnswerSettingDialogChanged
                            )
                            EditNumberListItem(
                                label = stringResource(id = R.string.position_start),
                                value = uiState.startPosition + 1,
                                valueString = stringResource(
                                    id = R.string.position_start_value,
                                    uiState.startPosition + 1
                                ),
                                onValueChanged = {
                                    answerSettingViewModel.onStartPositionChanged(it - 1)
                                }
                            )
                            EditNumberListItem(
                                label = stringResource(id = R.string.number_questions),
                                value = uiState.questionCount,
                                valueString = stringResource(
                                    id = R.string.question_count_value,
                                    uiState.questionCount
                                ),
                                onValueChanged = answerSettingViewModel::onQuestionCountChanged
                            )
                            ContainedWideButton(
                                modifier = Modifier.padding(4.dp),
                                onClick = {
                                    homeViewModel.onStartAnswerClicked(
                                        workbookId = requireLongArgument(ARG_WORKBOOK_ID)
                                    )
                                    dismiss()
                                },
                                text = stringResource(id = R.string.start)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        answerSettingViewModel.setup()
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun CheckboxListItem(
        label: String,
        checked: Boolean,
        onCheckedChanged: (Boolean) -> Unit
    ) {
        ListItem(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clickable {
                    onCheckedChanged(!checked)
                },
            text = { Text(text = label) },
            trailing = {
                Switch(
                    checked = checked,
                    onCheckedChange = onCheckedChanged
                )
            }
        )
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun EditNumberListItem(
        label: String,
        value: Int,
        valueString: String,
        onValueChanged: (Int) -> Unit
    ) {
        ListItem(
            modifier = Modifier.clickable {
                EditTextDialogFragment.newInstance(
                    title = label,
                    defaultText = value.toString(),
                    hint = "",
                    inputType = InputType.TYPE_CLASS_NUMBER
                )
                { newValue ->
                    onValueChanged(newValue.toInt())
                }.show(requireActivity().supportFragmentManager, "TAG")
            },
            text = {
                Row {
                    Text(text = label)
                    Spacer(modifier = Modifier.weight(weight = 1f))
                    Text(text = valueString)
                }
            }
        )
    }
}

