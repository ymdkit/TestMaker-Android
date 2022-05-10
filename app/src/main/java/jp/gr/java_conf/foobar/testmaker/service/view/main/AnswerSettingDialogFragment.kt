package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.example.core.QuestionCondition
import com.example.ui.answer.AnswerSettingViewModel
import com.example.ui.core.CheckboxListItem
import com.example.ui.core.ContainedWideButton
import com.example.ui.core.EditTextListItem
import com.example.ui.home.HomeViewModel
import com.example.ui.theme.TestMakerAndroidTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireLongArgument
import jp.gr.java_conf.foobar.testmaker.service.extensions.requireStringArgument

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

    // todo 画面表示時の初期の高さを調整する
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
}

