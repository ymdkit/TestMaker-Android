package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.core.QuestionCondition
import com.example.ui.core.CheckboxListItem
import com.example.ui.core.ClickableListItem
import com.example.ui.core.SectionHeaderListItem
import com.example.ui.preference.PreferenceViewModel
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme

@AndroidEntryPoint
class NewSettingsFragment : Fragment() {

    private val preferenceViewModel: PreferenceViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val uiState by preferenceViewModel.uiState.collectAsState()

                TestMakerAndroidTheme {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.setting)
                            )
                        },
                        content = {
                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                item {
                                    SectionHeaderListItem(
                                        text = stringResource(id = R.string.way)
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.is_random),
                                        checked = uiState.answerSetting.isRandomOrder,
                                        onCheckedChanged = preferenceViewModel::onIsRandomOrderChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.message_wrong_only),
                                        checked = uiState.answerSetting.questionCondition == QuestionCondition.WRONG,
                                        onCheckedChanged = preferenceViewModel::onQuestionConditionChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.message_switch_question),
                                        checked = uiState.answerSetting.isSwapProblemAndAnswer,
                                        onCheckedChanged = preferenceViewModel::onIsSwapProblemAndAnswerChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.message_self),
                                        checked = uiState.answerSetting.isSelfScoring,
                                        onCheckedChanged =
                                        preferenceViewModel::onIsSelfScoringChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.always_review),
                                        checked = uiState.answerSetting.isAlwaysShowExplanation,
                                        onCheckedChanged = preferenceViewModel::onIsAlwaysShowExplanationChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.setting_sound),
                                        checked = uiState.answerSetting.isPlaySound,
                                        onCheckedChanged = preferenceViewModel::onIsPlaySoundChanged
                                    )
                                }
                                item {
                                    CheckboxListItem(
                                        label = stringResource(id = R.string.setting_show_dialog),
                                        checked = uiState.answerSetting.isShowAnswerSettingDialog,
                                        onCheckedChanged = preferenceViewModel::onIsShowAnswerSettingDialogChanged
                                    )
                                }
                                item {
                                    EditNumberListItem(
                                        label = stringResource(id = R.string.position_start),
                                        value = uiState.answerSetting.startPosition + 1,
                                        valueString = stringResource(
                                            id = R.string.position_start_value,
                                            uiState.answerSetting.startPosition + 1
                                        ),
                                        onValueChanged = {
                                            preferenceViewModel.onStartPositionChanged(it - 1)
                                        }
                                    )
                                }
                                item {
                                    EditNumberListItem(
                                        label = stringResource(id = R.string.number_questions),
                                        value = uiState.answerSetting.questionCount,
                                        valueString = stringResource(
                                            id = R.string.question_count_value,
                                            uiState.answerSetting.questionCount
                                        ),
                                        onValueChanged = preferenceViewModel::onQuestionCountChanged
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                item {
                                    SectionHeaderListItem(
                                        text = stringResource(id = R.string.preference_group_account)
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.login),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.logout),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.setting_user_name),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                                item {
                                    SectionHeaderListItem(
                                        text = stringResource(id = R.string.preference_group_other)
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.action_remove_ad),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.preference_group_study_plus),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.help),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.menu_feedback),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
                                }
                                item {
                                    ClickableListItem(
                                        text = stringResource(id = R.string.action_license),
                                        onClick = preferenceViewModel::onLoginButtonClicked
                                    )
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
        preferenceViewModel.setup()
    }

    // todo Fragment への依存をはがし、再利用できる Composable にする
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

@Composable
fun TestMakerTopAppBar(
    title: String
) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        backgroundColor = Color.Transparent,
        elevation = 0.dp,
    )
}
