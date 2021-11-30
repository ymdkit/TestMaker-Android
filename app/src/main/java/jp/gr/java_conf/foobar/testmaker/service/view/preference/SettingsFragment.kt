package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import jp.gr.java_conf.foobar.testmaker.service.BuildConfig
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.studyplus.android.sdk.Studyplus
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    private val auth: Auth by inject()
    private val studyPlus by inject<Studyplus>()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initFontSizePreferences()
        initAccountPreferences()
        initStudyPlusPreferences()
        initOtherPreferences()
    }

    private fun initAccountPreferences() {
        val user = auth.getUser() ?: run { return@initAccountPreferences }

        val preference = findPreference<Preference>("preference_group_account")
        preference?.apply {
            isVisible = true
        }

        val userNamePreference = findPreference<Preference>("setting_user_name")
        userNamePreference?.apply {
            setOnPreferenceClickListener {

                EditTextDialogFragment.newInstance(
                        title = getString(R.string.title_dialog_edit_user_name),
                        defaultText = user.displayName ?: "",
                        hint = getString(R.string.hint_user_name))
                { text ->

                    auth.updateProfile(text) {
                        requireContext().showToast(getString(R.string.msg_update_user_name))
                        summaryProvider = Preference.SummaryProvider<Preference> {
                            text
                        }
                    }

                }.show(requireActivity().supportFragmentManager, "TAG")
                true
            }
            summaryProvider = Preference.SummaryProvider<Preference> {
                user.displayName
            }
        }

        val logoutPreference = findPreference<Preference>("setting_logout")
        logoutPreference?.apply {
            setOnPreferenceClickListener {
                MaterialAlertDialogBuilder(requireContext())
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.msg_logout))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            auth.logOut()
                            MainActivity.startActivityWithClear(requireActivity())
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
                true
            }
        }
    }

    private fun initFontSizePreferences() {
        val preference = findPreference<Preference>("play_font_size")
        preference?.apply {
            summaryProvider = Preference.SummaryProvider<ListPreference> {
                it.entry
            }
        }
    }

    fun initStudyPlusPreferences() {
        val studyPlusPreference = findPreference<Preference>("setting_study_plus")
        studyPlusPreference?.apply {
            summaryProvider = Preference.SummaryProvider<Preference> {
                if (studyPlus.isAuthenticated()) "連携中" else "未連携"
            }

            setOnPreferenceClickListener {
                try {
                    studyPlus.startAuth(requireActivity(), REQUEST_CODE_AUTH)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), getString(R.string.msg_download_study_plus), Toast.LENGTH_LONG).show()
                }
                true
            }
        }

        val studyPlusPostPreference = findPreference<ListPreference>("study_plus")
        studyPlusPostPreference?.apply {
            if (studyPlus.isAuthenticated()) {
                isVisible = true
                summaryProvider = Preference.SummaryProvider<ListPreference> {
                    it.entry
                }
            }
        }
    }

    private fun initOtherPreferences() {

        val feedbackPreference = findPreference<Preference>("feedback")
        feedbackPreference?.apply {
            setOnPreferenceClickListener {
                val emailIntent = Intent(Intent.ACTION_SENDTO)
                emailIntent.data = Uri.parse("mailto:")
                emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
                emailIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    getString(R.string.email_subject_feedback)
                )
                emailIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    getString(R.string.email_body_feedback, Build.VERSION.SDK_INT)
                )
                startActivity(Intent.createChooser(emailIntent, null))
                true
            }
        }

        val licensePreference = findPreference<Preference>("license")
        licensePreference?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(requireContext(), OssLicensesMenuActivity::class.java))
                true
            }
        }

        val versionPreference = findPreference<Preference>("version")
        versionPreference?.apply {
            summaryProvider = Preference.SummaryProvider<Preference> {
                BuildConfig.VERSION_NAME
            }
        }
    }

    companion object {
        const val REQUEST_CODE_AUTH = 1
    }
}