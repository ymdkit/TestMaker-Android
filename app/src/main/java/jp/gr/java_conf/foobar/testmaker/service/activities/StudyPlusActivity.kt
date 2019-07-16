package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.studyplus.android.sdk.Studyplus
import kotlinx.android.synthetic.main.activity_study_plus.*

class StudyPlusActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study_plus)

        initToolBar()

        connect_study_plus.setOnClickListener {

            try {
                Studyplus.instance.startAuth(this@StudyPlusActivity, REQUEST_CODE_AUTH)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                Toast.makeText(baseContext, getString(R.string.msg_download_study_plus), Toast.LENGTH_LONG).show()
            }

        }

        if (Studyplus.instance.isAuthenticated(baseContext)) {
            text_state_study_plus.text = getString(R.string.connected)
        } else {
            card_setting_study_plus.visibility = View.GONE
        }

        text_state_upload_study_plus.text = resources.getStringArray(R.array.upload_setting_study_plus)[sharedPreferenceManager.uploadStudyPlus]

        button_setting_upload_study_plus.setOnClickListener {

            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setTitle(getString(R.string.setting_study_plus_dialog))
                    .setItems(resources.getStringArray(R.array.upload_setting_study_plus)) { _, which ->

                        sharedPreferenceManager.uploadStudyPlus = which

                        text_state_upload_study_plus.text = resources.getStringArray(R.array.upload_setting_study_plus)[sharedPreferenceManager.uploadStudyPlus]
                    }.show()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_AUTH -> {

                Studyplus.instance.setAuthResult(this, data)
                Toast.makeText(this@StudyPlusActivity, getString(R.string.msg_connect_success), Toast.LENGTH_LONG).show()

                text_state_study_plus.text = getString(R.string.connected)
                card_setting_study_plus.visibility = View.VISIBLE

            }
        }
    }

    companion object {
        const val REQUEST_CODE_AUTH = 1
    }
}
