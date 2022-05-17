package com.example.ui.preference

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.example.core.QuestionCondition
import com.example.core.StudyPlusSetting
import com.example.ui.R
import com.example.ui.core.*
import com.example.ui.core.item.*
import com.example.ui.theme.TestMakerAndroidTheme
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val preferenceViewModel: PreferenceViewModel by viewModels()
    private val purchaseViewModel: PurchaseViewModel by viewModels()
    private val adViewModel: AdViewModel by viewModels()

    @Inject
    lateinit var colorMapper: ColorMapper

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val launcher =
                    rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) {
                        preferenceViewModel.onUserCreated()
                    }

                val uiState by preferenceViewModel.uiState.collectAsState()
                val answerSetting = uiState.answerSetting
                val user = uiState.user

                TestMakerAndroidTheme(
                    themeColor = uiState.themeColor
                ) {
                    Scaffold(
                        topBar = {
                            TestMakerTopAppBar(
                                title = stringResource(id = R.string.setting)
                            )
                        },
                        content = {
                            Column {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    item {
                                        SectionHeaderListItem(
                                            text = stringResource(id = R.string.way)
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.is_random),
                                            checked = answerSetting.isRandomOrder,
                                            onCheckedChanged = preferenceViewModel::onIsRandomOrderChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.message_wrong_only),
                                            checked = answerSetting.questionCondition == QuestionCondition.WRONG,
                                            onCheckedChanged = preferenceViewModel::onQuestionConditionChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.message_switch_question),
                                            checked = answerSetting.isSwapProblemAndAnswer,
                                            onCheckedChanged = preferenceViewModel::onIsSwapProblemAndAnswerChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.message_self),
                                            checked = answerSetting.isSelfScoring,
                                            onCheckedChanged =
                                            preferenceViewModel::onIsSelfScoringChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.always_review),
                                            checked = answerSetting.isAlwaysShowExplanation,
                                            onCheckedChanged = preferenceViewModel::onIsAlwaysShowExplanationChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.setting_sound),
                                            checked = answerSetting.isPlaySound,
                                            onCheckedChanged = preferenceViewModel::onIsPlaySoundChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.setting_is_case_insensitive),
                                            checked = answerSetting.isCaseInsensitive,
                                            onCheckedChanged = preferenceViewModel::onIsCaseInsensitiveChanged
                                        )
                                    }
                                    item {
                                        CheckboxListItem(
                                            label = stringResource(id = R.string.setting_show_dialog),
                                            checked = answerSetting.isShowAnswerSettingDialog,
                                            onCheckedChanged = preferenceViewModel::onIsShowAnswerSettingDialogChanged
                                        )
                                    }
                                    item {
                                        EditTextListItem(
                                            label = stringResource(id = R.string.position_start),
                                            value = (answerSetting.startPosition + 1).toString(),
                                            keyboardType = KeyboardType.Number,
                                            dialogTitle = stringResource(id = R.string.position_start),
                                            onValueSubmitted = {
                                                preferenceViewModel.onStartPositionChanged(it.toInt() - 1)
                                            },
                                            validate = {
                                                it.isNotEmpty() && it.toIntOrNull() != null && it.toInt() >= 0
                                            }
                                        )
                                    }
                                    item {
                                        EditTextListItem(
                                            label = stringResource(id = R.string.number_questions),
                                            value = answerSetting.questionCount.toString(),
                                            keyboardType = KeyboardType.Number,
                                            dialogTitle = stringResource(id = R.string.number_questions),
                                            onValueSubmitted = {
                                                preferenceViewModel.onQuestionCountChanged(it.toInt())
                                            },
                                            validate = {
                                                it.isNotEmpty() && it.toIntOrNull() != null && it.toInt() >= 0
                                            }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(16.dp))
                                    }
                                    item {
                                        SectionHeaderListItem(
                                            text = stringResource(id = R.string.preference_group_appearance)
                                        )
                                    }
                                    item {
                                        ColorDropDownListItem(
                                            label = stringResource(id = R.string.prefrence_theme_color),
                                            value = colorMapper.colorToLabel(uiState.themeColor),
                                            colorMapper = colorMapper,
                                            onValueChange = preferenceViewModel::onThemeColorChanged
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
                                    if (user != null) {
                                        item {
                                            EditTextListItem(
                                                label = stringResource(id = R.string.setting_user_name),
                                                value = user.displayName,
                                                dialogTitle = stringResource(id = R.string.dialog_edit_display_name),
                                                onValueSubmitted = preferenceViewModel::onDisplayNameSubmitted,
                                            )
                                        }
                                        item {
                                            ConfirmActionListItem(
                                                label = stringResource(id = R.string.logout),
                                                confirmMessage = stringResource(id = R.string.confirm_logout),
                                                confirmButtonText = stringResource(id = R.string.button_logout),
                                                onConfirmed = preferenceViewModel::onLogoutButtonClicked
                                            )
                                        }
                                    } else {
                                        item {
                                            ClickableListItem(
                                                text = stringResource(id = R.string.login),
                                                onClick = {
                                                    launcher.launch(authUIIntent())
                                                }
                                            )
                                        }
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
                                            onClick = {
                                                if (!adViewModel.isRemovedAd.value) {
                                                    purchaseViewModel.launchBillingFlow(
                                                        activity = requireActivity(),
                                                        billingItem = BillingItem(
                                                            getString(R.string.sku_remove_ad),
                                                            BillingClient.SkuType.INAPP
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    if (uiState.isStudyPlusAuthenticated) {
                                        item {
                                            PickerListItem(
                                                text = stringResource(id = R.string.preference_post_study_plus),
                                                secondaryText = StudyPlusSetting.fromValue(uiState.studyPlusSetting).label,
                                                itemList = StudyPlusSetting.values()
                                                    .map { it.label to it.value },
                                                onSelected = preferenceViewModel::onStudyPlusSettingChanged
                                            )
                                        }
                                    }
                                    item {
                                        ClickableListItem(
                                            text = stringResource(id = R.string.help),
                                            onClick = {
                                                startActivity(Intent(Intent.ACTION_VIEW).apply {
                                                    data = Uri.parse("https://ankimaker.com/guide")
                                                })
                                            }
                                        )
                                    }
                                    item {
                                        ClickableListItem(
                                            text = stringResource(id = R.string.menu_feedback),
                                            onClick = {
                                                startActivity(feedbackIntent())
                                            }
                                        )
                                    }
                                    item {
                                        ClickableListItem(
                                            text = stringResource(id = R.string.action_license),
                                            onClick = {
                                                startActivity(
                                                    Intent(
                                                        requireContext(),
                                                        OssLicensesMenuActivity::class.java
                                                    )
                                                )
                                            }
                                        )
                                    }
                                    item {
                                        ClickableListItem(
                                            text = stringResource(id = R.string.version_app),
                                            secondaryText = getAppVersion(),
                                            onClick = {}
                                        )
                                    }
                                }
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
        preferenceViewModel.setup()
        purchaseViewModel.setup()
        adViewModel.setup()

        lifecycleScope.launchWhenCreated {
            preferenceViewModel.logoutEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.msg_success_logout))
                }
                .launchIn(this)

            purchaseViewModel.uiState
                .onEach {
                    when (it) {
                        is BillingUiState.Error -> {
                            when (it.responseCode) {
                                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                                    Toast.makeText(
                                        requireContext(),
                                        getString(R.string.alrady_removed_ad),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    preferenceViewModel.onAdRemoved()
                                }
                                BillingClient.BillingResponseCode.USER_CANCELED -> Toast.makeText(
                                    requireContext(),
                                    getString(R.string.purchase_canceled),
                                    Toast.LENGTH_SHORT
                                ).show()
                                else -> Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        is BillingUiState.PurchaseSuccess -> {
                            it.purchases?.let {
                                for (purchase in it) {
                                    val sku = purchase.skus.firstOrNull() ?: return@let
                                    when (sku) {
                                        getString(R.string.sku_remove_ad) -> {
                                            requireContext().showToast(
                                                getString(R.string.msg_remove_ad_success),
                                                Toast.LENGTH_LONG
                                            )
                                            preferenceViewModel.onAdRemoved()
                                        }
                                    }
                                }
                            }
                        }
                        else -> {
                            // do nothing
                        }
                    }
                }.launchIn(this)
        }
    }

    private fun authUIIntent() =
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                arrayListOf(
                    AuthUI.IdpConfig.EmailBuilder().build(),
                    AuthUI.IdpConfig.GoogleBuilder().build()
                )
            )
            .setTosAndPrivacyPolicyUrls(
                "https://ankimaker.com/terms",
                "https://ankimaker.com/privacy"
            )
            .build()

    private fun feedbackIntent() =
        Intent.createChooser(Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
            putExtra(
                Intent.EXTRA_SUBJECT,
                getString(R.string.email_subject_feedback)
            )
            putExtra(
                Intent.EXTRA_TEXT,
                getString(
                    R.string.email_body_feedback,
                    Build.VERSION.SDK_INT.toString(),
                    getAppVersion()
                )
            )
        }, null)

    private fun getAppVersion() =
        requireContext()
            .packageManager
            .getPackageInfo(requireContext().packageName, 0)
            .versionName

}
