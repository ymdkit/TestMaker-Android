package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import jp.gr.java_conf.foobar.testmaker.service.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}