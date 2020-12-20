package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.gr.java_conf.foobar.testmaker.service.BuildConfig
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.studyplus.android.sdk.Studyplus
import org.koin.android.ext.android.inject

class SettingsFragment : PreferenceFragmentCompat() {

    private val auth: Auth by inject()

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
                val dialogLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_edit_user_name, requireActivity().findViewById(R.id.layout_dialog_edit_user))
                val editUsername = dialogLayout.findViewById<EditText>(R.id.edit_user_name)
                editUsername.setText(user.displayName)
                val buttonSaveProfile = dialogLayout.findViewById<Button>(R.id.button_save_profile)
                val dialog = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                        .setView(dialogLayout)
                        .setTitle(getString(R.string.message_edit_profile))
                        .show()

                buttonSaveProfile.setOnClickListener {
                    val userName = editUsername.text.toString()
                    if (userName.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.message_shortage), Toast.LENGTH_SHORT).show()
                    } else {
                        auth.updateProfile(userName) {
                            requireContext().showToast(getString(R.string.msg_update_user_name))
                            summaryProvider = Preference.SummaryProvider<Preference> {
                                userName
                            }
                        }
                        dialog.dismiss()
                    }
                }
                true
            }
            summaryProvider = Preference.SummaryProvider<Preference> {
                user.displayName
            }
        }

        val logoutPreference = findPreference<Preference>("setting_logout")
        logoutPreference?.apply {
            setOnPreferenceClickListener {
                AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
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
                if (Studyplus.instance.isAuthenticated(requireContext())) "連携中" else "未連携"
            }

            setOnPreferenceClickListener {
                try {
                    Studyplus.instance.startAuth(requireActivity(), REQUEST_CODE_AUTH)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), getString(R.string.msg_download_study_plus), Toast.LENGTH_LONG).show()
                }
                true
            }
        }

        val studyPlusPostPreference = findPreference<ListPreference>("study_plus")
        studyPlusPostPreference?.apply {
            if (Studyplus.instance.isAuthenticated(requireContext())) {
                isVisible = true
                summaryProvider = Preference.SummaryProvider<ListPreference> {
                    it.entry
                }
            }
        }
    }

    private fun initOtherPreferences() {
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