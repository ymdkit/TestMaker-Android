package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.widget.Toast
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.studyplus.android.sdk.Studyplus

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        initStudyPlusPreferences()
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

    companion object {
        const val REQUEST_CODE_AUTH = 1
    }
}