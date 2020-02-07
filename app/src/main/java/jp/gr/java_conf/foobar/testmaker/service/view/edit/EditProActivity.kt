package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditProBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTestAsync
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.android.synthetic.main.activity_edit_pro.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditProActivity : BaseActivity() {

    private val editProViewModel: EditProViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pro)

        val binding = DataBindingUtil.setContentView<ActivityEditProBinding>(this, R.layout.activity_edit_pro)
        createAd(binding.adView)

        initToolBar()

        button_save.setOnClickListener {
            if (sharedPreferenceManager.confirmSave) {
                saveText()
                return@setOnClickListener
            }

            val dialogLayout = LayoutInflater.from(this@EditProActivity).inflate(R.layout.dialog_alert_confirm, findViewById(R.id.layout_dialog_confirm))
            val checkBox = dialogLayout.findViewById<CheckBox>(R.id.check_alert)

            AlertDialog.Builder(this@EditProActivity, R.style.MyAlertDialogStyle)
                    .setView(dialogLayout)
                    .setTitle(getString(R.string.confirm))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        if (checkBox.isChecked) sharedPreferenceManager.confirmSave = true
                        saveText()
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()

        }

        edit_test.setText(editProViewModel.getTest(intent.getLongExtra("testId", -1)).testToString(this, false))

    }

    private fun saveText() {

        val text = edit_test.text.toString()

        val questionId = editProViewModel.getMaxQuestionId()

        lifecycleScope.launch {

            val test = text.toTestAsync(baseContext, questionId)

            withContext(Dispatchers.Main) {
                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_update), Toast.LENGTH_LONG).show()
                test.id = intent.getLongExtra("testId", -1)
                editProViewModel.addOrUpdateTest(test)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit_pro, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val actionId = item.itemId

        if (actionId == android.R.id.home) {

            finish()

            return true

        } else if (actionId == R.id.nav_help) {

            startActivity(Intent(Intent.ACTION_VIEW, Uri
                    .parse(getString(R.string.help_url))))

        }

        return super.onOptionsItemSelected(item)
    }

}