package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityUploadTestBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.UploadTestDestination
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.LocalMainFragment
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadTestActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()
    private val viewModel: FirebaseViewModel by viewModel()
    private val logger: TestMakerLogger by inject()
    private var ablePrivateUpload = false

    private val binding: ActivityUploadTestBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_upload_test
        )
    }

    private val signIn = registerForActivityResult(SignInRequestContract()){
        it ?: run {
            finish()
            return@registerForActivityResult
        }
        showToast(getString(R.string.msg_success_login))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAd(binding.adView)
        loadRewardedAd()

        viewModel.getUser() ?: run {

            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    signIn.launch(Unit)
                }
                .setNegativeButton(getString(R.string.cancel)) { _, _ ->
                    finish()
                }
                .setOnCancelListener {
                    finish()
                }
                .show()
        }

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            testViewModel.tests.map { it.title }.toTypedArray()
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner.adapter = adapter

        val index =
            testViewModel.tests.indexOfFirst { it.id == intent.getLongExtra(ARGUMENT_ID, 0) }
                .coerceAtLeast(0)
        binding.spinner.setSelection(index)
        initToolBar()

        binding.buttonUpload.setOnClickListener {

            lifecycleScope.launch(Dispatchers.Default) {

                viewModel.isAlreadyUploaded(testViewModel.tests[binding.spinner.selectedItemPosition].title)
                    ?.let {

                        withContext(Dispatchers.Main) {

                            ListDialogFragment.newInstance(
                                getString(R.string.upload_dialog_already_uploaded),
                                listOf(
                                    DialogMenuItem(
                                        title = getString(R.string.upload_dialog_overwrite),
                                        iconRes = R.drawable.ic_baseline_update_24,
                                        action = {
                                            overwriteTest(it.documentId)
                                        }),
                                    DialogMenuItem(
                                        title = getString(R.string.upload_dialog_upload_as_other_test),
                                        iconRes = R.drawable.ic_baseline_file_copy_24,
                                        action = {
                                            uploadTest()
                                        }),
                                )
                            ).show(supportFragmentManager, "TAG")
                        }
                    } ?: run {
                    withContext(Dispatchers.Main) {
                        uploadTest()
                    }
                }
            }
        }

        binding.checkPrivate.setOnCheckedChangeListener { checkBox, isChecked ->
            if (sharedPreferenceManager.isRemovedAd) return@setOnCheckedChangeListener
            if (ablePrivateUpload) return@setOnCheckedChangeListener

            if (isChecked) {
                checkBox.isChecked = false

                AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setTitle(getString(R.string.title_dialog_private))
                    .setMessage(getString(R.string.msg_dialog_private))
                    .setPositiveButton(android.R.string.ok) { dialog, _ ->

                        if (!isConnectedInternet()) {
                            showToast(getString(R.string.client_network_error))
                            return@setPositiveButton
                        }

                        lifecycleScope.launch(Dispatchers.Main) {
                            showProgress(getString(R.string.load_ad))
                            var count = 0L
                            while (count <= LIMIT_AD_ATTEMPT_NUM) {
                                if (rewardedAd.isLoaded) {
                                    val activityContext: Activity = this@UploadTestActivity
                                    val adCallback = object : RewardedAdCallback() {
                                        override fun onRewardedAdClosed() {
                                            loadRewardedAd()
                                        }

                                        override fun onUserEarnedReward(reword: RewardItem) {
                                            ablePrivateUpload = true
                                            binding.checkPrivate.isChecked = true
                                            dialog.dismiss()
                                        }
                                    }
                                    rewardedAd.show(activityContext, adCallback)
                                    break
                                } else {
                                    count += 1
                                    delay(count * 1000)
                                }
                            }
                            hideProgress()
                        }
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()
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

    fun uploadTest() {

        executeJobWithDialog(
            title = getString(R.string.uploading),
            task = {
                viewModel.uploadTest(
                    RealmTest.createFromTest(testViewModel.tests[binding.spinner.selectedItemPosition]),
                    binding.editOverview.text.toString(),
                    !binding.checkPrivate.isChecked
                )
            },
            onSuccess = { documentId ->
                logger.logUploadTestEvent(
                    test = testViewModel.tests[binding.spinner.selectedItemPosition],
                    destination = if (ablePrivateUpload) UploadTestDestination.PRIVATE.title else UploadTestDestination.PUBLIC.title
                )
                if (intent.hasExtra(ARGUMENT_ID)) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(
                            LocalMainFragment.EXTRA_TEST_NAME,
                            testViewModel.tests[binding.spinner.selectedItemPosition].title
                        )
                        putExtra(LocalMainFragment.EXTRA_DOCUMENT_ID, documentId)
                    })
                }
                Toast.makeText(
                    baseContext,
                    getString(R.string.msg_test_upload),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            },
            onFailure = {
                Toast.makeText(
                    baseContext,
                    getString(R.string.msg_canceled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    fun overwriteTest(id: String) {

        executeJobWithDialog(
            title = getString(R.string.uploading),
            task = {
                viewModel.overwriteTest(
                    id,
                    RealmTest.createFromTest(testViewModel.tests[binding.spinner.selectedItemPosition]),
                    binding.editOverview.text.toString(),
                    !binding.checkPrivate.isChecked
                )
            },
            onSuccess = { documentId ->
                logger.logUploadTestEvent(
                    test = testViewModel.tests[binding.spinner.selectedItemPosition],
                    destination = if (ablePrivateUpload) UploadTestDestination.PRIVATE.title else UploadTestDestination.PUBLIC.title
                )
                if (intent.hasExtra(ARGUMENT_ID)) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(
                            LocalMainFragment.EXTRA_TEST_NAME,
                            testViewModel.tests[binding.spinner.selectedItemPosition].title
                        )
                        putExtra(LocalMainFragment.EXTRA_DOCUMENT_ID, documentId)
                    })
                }
                Toast.makeText(
                    baseContext,
                    getString(R.string.msg_test_upload),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            },
            onFailure = {
                Toast.makeText(
                    baseContext,
                    it.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun isConnectedInternet(): Boolean{
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.getNetworkCapabilities(cm.activeNetwork) != null
    }

    companion object {
        const val LIMIT_AD_ATTEMPT_NUM = 3L
        const val ARGUMENT_ID = "id"

        fun startActivity(activity: Activity) {
            activity.startActivity(Intent(activity, UploadTestActivity::class.java))
        }

        fun startActivityForResult(fragment: Fragment, requestCode: Int, id: Long) {
            val intent = Intent(fragment.requireContext(), UploadTestActivity::class.java).apply {
                putExtra(ARGUMENT_ID, id)
            }
            fragment.startActivityForResult(intent, requestCode)
        }
    }
}