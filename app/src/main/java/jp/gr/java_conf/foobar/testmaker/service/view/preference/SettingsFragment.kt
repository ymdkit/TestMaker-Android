package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import jp.gr.java_conf.foobar.testmaker.service.BuildConfig
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.studyplus.android.sdk.Studyplus

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initFontSizePreferences()
        initStudyPlusPreferences()
        initOtherPreferences()
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