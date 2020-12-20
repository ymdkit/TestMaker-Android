package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityUploadTestBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadTestActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()
    private val viewModel: FirebaseViewModel by viewModel()

    private val binding: ActivityUploadTestBinding by lazy {
        DataBindingUtil.setContentView<ActivityUploadTestBinding>(this, R.layout.activity_upload_test)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAd(binding.adView)

        val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, testViewModel.tests.map { it.title }.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter
        initToolBar()

        binding.buttonUpload.setOnClickListener {
            lifecycleScope.launch {
                val progress = AlertDialog.Builder(this@UploadTestActivity)
                        .setTitle(getString(R.string.uploading))
                        .setView(LayoutInflater.from(this@UploadTestActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

                viewModel.uploadTest(RealmTest.createFromTest(testViewModel.tests[binding.spinner.selectedItemPosition]), binding.editOverview.text.toString())

                Toast.makeText(baseContext, getString(R.string.msg_test_upload), Toast.LENGTH_SHORT).show()
                progress.dismiss()
                finish()

            }
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

    companion object {
        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, UploadTestActivity::class.java))
        }
    }
}