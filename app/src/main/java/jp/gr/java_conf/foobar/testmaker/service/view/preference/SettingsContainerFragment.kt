package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.android.gms.ads.AdRequest
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentSettingsContainerBinding
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.studyplus.android.sdk.Studyplus
import javax.inject.Inject


@AndroidEntryPoint
class SettingsContainerFragment : Fragment() {

    @Inject
    lateinit var studyPlus: Studyplus

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentSettingsContainerBinding>(
            inflater,
            R.layout.fragment_settings_container,
            container,
            false
        )

        if (sharedPreferenceManager.isRemovedAd) {
            binding.adView.visibility = View.GONE
        } else {
            binding.adView.loadAd(AdRequest.Builder().build())
        }

        childFragmentManager
            .beginTransaction()
            .replace(R.id.settingsContainer, SettingsFragment())
            .commit()

        return binding.root
    }

    fun setStudyPlusAuthResult(data: Intent){
        studyPlus.setAuthResult(data)
        Toast.makeText(requireContext(), getString(R.string.msg_connect_success), Toast.LENGTH_LONG).show()

        childFragmentManager.findFragmentById(R.id.settingsContainer)?.apply {

            if (this is SettingsFragment) {
                this.initStudyPlusPreferences()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SettingsFragment.REQUEST_CODE_AUTH -> {
                if (resultCode == AppCompatActivity.RESULT_OK && data != null) {

                    studyPlus.setAuthResult(data)
                    Toast.makeText(requireContext(), getString(R.string.msg_connect_success), Toast.LENGTH_LONG).show()

                    childFragmentManager.findFragmentById(R.id.settingsContainer)?.apply {

                        if (this is SettingsFragment) {
                            this.initStudyPlusPreferences()
                        }
                    }
                }
            }
        }

    }

}