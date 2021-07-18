package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.firebase.ui.auth.IdpResponse
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityOnlineMainBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class FirebaseActivity : BaseActivity() {

    private val viewModel: FirebaseViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    private val logger: TestMakerLogger by inject()

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
            override fun onClickTest(test: FirebaseTest) {

                ListDialogFragment(
                        test.name,
                        listOf(
                                DialogMenuItem(title = getString(R.string.download), iconRes = R.drawable.ic_file_download_white, action = { downloadTest(test) }),
                                DialogMenuItem(title = getString(R.string.info), iconRes = R.drawable.ic_info_white, action = { showInfoTest(test) }),
                                DialogMenuItem(title = getString(R.string.report), iconRes = R.drawable.ic_baseline_flag_24, action = { reportTest(test) })
                        )
                ).show(supportFragmentManager, "TAG")

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
                logger.logEvent("upload_from_firebase_activity")
                UploadTestActivity.startActivity(this)
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
                logger.logSearchEvent(s)
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

    fun downloadTest(test: FirebaseTest) {

        executeJobWithDialog(
                title = getString(R.string.downloading),
                task = {
                    viewModel.downloadTest(test.documentId)
                },
                onSuccess = {
                    viewModel.convert(it)

                    Toast.makeText(this, getString(R.string.msg_success_download_test, it.name), Toast.LENGTH_SHORT).show()
                    logger.logCreateTestEvent(it.name, CreateTestSource.PUBLIC_DOWNLOAD.title)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                },
                onFailure = {
                    showToast(getString(R.string.msg_failure_download_test))
                }
        )
    }

    fun showInfoTest(test: FirebaseTest) {

        ListDialogFragment(
                test.name,
                listOf(
                        DialogMenuItem(title = getString(R.string.text_info_creator, test.userName), iconRes = R.drawable.ic_account, action = { }),
                        DialogMenuItem(title = getString(R.string.text_info_created_at, test.getDate()), iconRes = R.drawable.ic_baseline_calendar_today_24, action = { }),
                        DialogMenuItem(title = getString(R.string.text_info_overview, test.overview), iconRes = R.drawable.ic_baseline_description_24, action = { })
                )
        ).show(supportFragmentManager, "TAG")

    }

    fun reportTest(test: FirebaseTest) {

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_subject, test.documentId))
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_body))
        startActivity(Intent.createChooser(emailIntent, null))

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

    companion object {
        const val RC_SIGN_IN = 54321
    }

}
