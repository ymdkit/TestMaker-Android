package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.IdpResponse
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityOnlineMainBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FirebaseActivity : BaseActivity() {

    private val viewModel: FirebaseViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    private val controller: FirebaseTestController by lazy {
        FirebaseTestController(this)
    }

    private val binding: ActivityOnlineMainBinding by lazy {
        DataBindingUtil.setContentView<ActivityOnlineMainBinding>(this, R.layout.activity_online_main).also {
            it.viewModel = viewModel
            it.lifecycleOwner = this
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createAd(binding.adView)
        binding.recyclerView.adapter = controller.adapter

        initToolBar()

        viewModel.getTests()

        viewModel.tests.observeNonNull(this) {
            controller.tests = it
        }

        viewModel.error.observeNonNull(this) {
            showErrorToast(it)
        }

        controller.setOnClickListener(object : FirebaseTestController.OnClickListener {
            override fun onClickDownloadTest(test: FirebaseTest) {
                lifecycleScope.launch {

                    val dialog = AlertDialog.Builder(this@FirebaseActivity)
                            .setTitle(getString(R.string.downloading))
                            .setView(LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

                    when (val result = viewModel.downloadTest(test.documentId)) {
                        is FirebaseTestResult.Success -> {
                            viewModel.convert(result.test)

                            Toast.makeText(this@FirebaseActivity, getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@FirebaseActivity, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            startActivity(intent)
                        }
                        is FirebaseTestResult.Failure -> {
                            Toast.makeText(this@FirebaseActivity, result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()
                }
            }

            override fun onClickShowInfoTest(test: FirebaseTest) {
                val dialogLayout = LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

                val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
                textInfo.text = getString(R.string.info_firebase_test, test.userName, test.getDate(), test.overview)

                val buttonReport = dialogLayout.findViewById<Button>(R.id.button_report)
                buttonReport.setOnClickListener {

                    val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "testmaker.contact@gmail.com", null))
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject))
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_body))
                    startActivity(Intent.createChooser(emailIntent, null))
                }

                val builder = AlertDialog.Builder(this@FirebaseActivity, R.style.MyAlertDialogStyle)
                builder.setView(dialogLayout)
                builder.setTitle(test.name)
                builder.show()
            }
        })

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.getTests()
        }

        binding.fab.setOnClickListener {

            viewModel.getUser()?.let {

                if (testViewModel.tests.isEmpty() || testViewModel.tests.all { it.questions.isEmpty() }) {

                    Toast.makeText(baseContext, getString(R.string.message_non_exist_test), Toast.LENGTH_SHORT).show()

                    return@setOnClickListener
                }

                showDialogUpload()

            } ?: run {

                login()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_firebase, menu)
        val searchView = menu.findItem(R.id.menu_search).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                viewModel.getTests(s)
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                return false
            }
        })

        searchView.setOnCloseListener {
            viewModel.getTests()
            false
        }
        return true
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

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                viewModel.createUser(viewModel.getUser())

                Toast.makeText(this, getString(R.string.login_successed), Toast.LENGTH_SHORT).show()
                // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                response?.error?.errorCode
                // ...
            }
        }
    }

    private fun login() {

        AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    startActivityForResult(
                            viewModel.getAuthUIIntent(),
                            RC_SIGN_IN)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    private fun showDialogUpload() {

        var position = 0

        val dialogLayout = LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_upload, findViewById(R.id.layout_dialog_upload))

        val spinner = dialogLayout.findViewById<Spinner>(R.id.spinner)
        val editOverView = dialogLayout.findViewById<EditText>(R.id.edit_overview)
        val adapter = ArrayAdapter(baseContext,
                android.R.layout.simple_spinner_item, testViewModel.tests.map { it.title }.toTypedArray())

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, positionSpinner: Int, id: Long) {
                position = positionSpinner
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dialog = AlertDialog.Builder(this@FirebaseActivity, R.style.MyAlertDialogStyle)
                .setView(dialogLayout)
                .setTitle(getString(R.string.message_upload_test))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener { it ->
                    it.isEnabled = false

                    lifecycleScope.launch {

                        val progress = AlertDialog.Builder(this@FirebaseActivity)
                                .setTitle(getString(R.string.uploading))
                                .setView(LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

                        viewModel.uploadTest(RealmTest.createFromTest(testViewModel.tests[position]), editOverView.text.toString())

                        Toast.makeText(baseContext, getString(R.string.msg_test_upload), Toast.LENGTH_SHORT).show()
                        viewModel.getTests()
                        dialog.dismiss()
                        progress.dismiss()

                    }
                }
    }

    companion object {
        const val RC_SIGN_IN = 54321
    }

}
