package jp.gr.java_conf.foobar.testmaker.service.view.preference

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivitySettingsBinding
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.studyplus.android.sdk.Studyplus
import org.koin.android.ext.android.inject

class SettingsActivity : BaseActivity() {

    private val studyPlus by inject<Studyplus>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivitySettingsBinding>(this, R.layout.activity_settings)
        createAd(binding.adView)

        initToolBar()

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsContainer, SettingsFragment())
                .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //StudyPlusとの連携完了後、Fragmentにイベントを通知する
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SettingsFragment.REQUEST_CODE_AUTH -> {
                if (resultCode == RESULT_OK && data != null) {

                    studyPlus.setAuthResult(data)
                    Toast.makeText(baseContext, getString(R.string.msg_connect_success), Toast.LENGTH_LONG).show()

                    supportFragmentManager.findFragmentById(R.id.settingsContainer)?.apply {

                        if (this is SettingsFragment) {
                            this.initStudyPlusPreferences()
                        }
                    }
                }
            }
        }

    }
}
