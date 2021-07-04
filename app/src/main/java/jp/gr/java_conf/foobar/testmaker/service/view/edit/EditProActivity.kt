package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditProBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class EditProActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()
    private val service: CloudFunctionsService by inject()
    private val logger: TestMakerLogger by inject()

    private lateinit var test: Test

    private val binding: ActivityEditProBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_edit_pro
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_pro)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
        }

        createAd(binding.adView)

        initToolBar()

        binding.buttonSave.setOnClickListener {
            if (sharedPreferenceManager.confirmSave) {
                saveText()
                return@setOnClickListener
            }

            val dialogLayout = LayoutInflater.from(this@EditProActivity)
                .inflate(R.layout.dialog_alert_confirm, findViewById(R.id.layout_dialog_confirm))
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

        lifecycleScope.launch {
            showProgress()
            runCatching {
                withContext(Dispatchers.IO) {
                    service.testToText(test.escapedTest.copy(lang = if (Locale.getDefault().language == "ja") "ja" else "en"))
                }
            }.onSuccess {
                binding.editTest.setText(it.text)
            }.onFailure {
                showErrorToast(it)
            }
            hideProgress()
        }
    }

    private fun saveText() {
        logger.logEvent("edit_by_raw_text")
        val text = binding.editTest.text.toString()
        lifecycleScope.launch {
            showProgress()
            runCatching {
                withContext(Dispatchers.IO) {
                    service.textToTest(
                        text = text.replace("\n", "¥n").replace("<", "&lt;"),
                        lang = if (Locale.getDefault().language == "ja") "ja" else "en"
                    )
                }
            }.onSuccess {
                testViewModel.update(it.copy(id = test.id, order = test.order))
                showToast(getString(R.string.message_success_update))
            }.onFailure {
                showErrorToast(it)
            }
            hideProgress()

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

            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri
                        .parse(getString(R.string.help_url))
                )
            )

        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, EditProActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }
    }

}