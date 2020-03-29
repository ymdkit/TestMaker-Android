package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.AuthUI
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditActivity
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainController
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.play.NewPlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.SelfJudgePlayActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

open class ShowTestsActivity : BaseActivity() {

    internal lateinit var mainController: MainController

    private val showTestsViewModel: ShowTestsViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()
    private val categoryViewModel: CategoryViewModel by viewModel()

    private var selectedTest: RealmTest? = null //ログイン時に一度画面から離れるので選択中の値を保持

    protected fun initTestAndFolderAdapter() {

        mainController = MainController(this)
        mainController.setOnClickListener(object : MainController.OnClickListener {

            override fun onClickPlayTest(test: Test) {

                sendFirebaseEvent("play")

                if (test.questions.isEmpty()) {

                    Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show()

                } else {

                    initDialogPlayStart(test)

                }
            }

            override fun onClickEditTest(test: Test) {

                sendFirebaseEvent("edit")

                EditActivity.startActivity(this@ShowTestsActivity, test.id)

            }

            override fun onClickDeleteTest(test: Test) {

                sendFirebaseEvent("delete")
                val builder = AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
                builder.setTitle(getString(R.string.delete_exam))
                builder.setMessage(getString(R.string.message_delete_exam, test.title))
                builder.setPositiveButton(android.R.string.ok) { _, _ ->
                    testViewModel.delete(test)
                    categoryViewModel.refresh()
                }
                builder.setNegativeButton(android.R.string.cancel, null)
                builder.create().show()
            }

            override fun onClickShareTest(test: Test) {

                AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.title_dialog_share))
                        .setItems(resources.getStringArray(R.array.action_share)) { dialog, which ->

                            when (which) {
                                0 -> { //リンクの共有
                                    showTestsViewModel.getUser()?.let {
                                        dialog.dismiss()
                                        uploadTest(RealmTest.createFromTest(test))
                                    } ?: run {
                                        login(RealmTest.createFromTest(test))
                                    }
                                }

                                1 -> { //テキスト変換

                                    Toast.makeText(baseContext, getString(R.string.message_share_exam, test.title), Toast.LENGTH_LONG).show()

                                    try {
                                        val intent = Intent()
                                        intent.action = Intent.ACTION_SEND
                                        intent.type = "text/plain"

                                        intent.putExtra(Intent.EXTRA_TEXT, RealmTest.createFromTest(test).testToString(baseContext, false))
                                        startActivity(intent)

                                    } catch (e: Exception) {

                                        sendFirebaseEvent("export error: $e")

                                    }
                                }
                            }

                        }.show()
            }
        })
    }

    private fun uploadTest(test: RealmTest) {

        lifecycleScope.launch {

            val progress = AlertDialog.Builder(this@ShowTestsActivity)
                    .setTitle(getString(R.string.uploading))
                    .setView(LayoutInflater.from(this@ShowTestsActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).create()

            progress.show()

            val documentId = showTestsViewModel.uploadTest(test, test.documentId)

            progress.dismiss()
            val dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("https://testmaker-1cb29.com/$documentId"))
                    .setDomainUriPrefix("https://testmaker.page.link")
                    .setAndroidParameters(DynamicLink.AndroidParameters.Builder().setMinimumVersion(87).build())
                    .setIosParameters(DynamicLink.IosParameters.Builder("jp.gr.java-conf.foobar.testmaker.service").setAppStoreId("1201200202").setMinimumVersion("2.1.5").build())
                    .buildDynamicLink()

            try {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"

                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_test, test.title, dynamicLink.uri))
                startActivity(intent)

            } catch (e: Exception) {
                sendFirebaseEvent("export error: $e")
            }

        }
    }

    private fun initDialogPlayStart(test: Test) {

        val dialogLayout = LayoutInflater.from(this@ShowTestsActivity).inflate(R.layout.dialog_start, findViewById(R.id.layout_dialog_start))

        val editLimit = dialogLayout.findViewById<EditText>(R.id.set_limit)
        editLimit.setText(test.limit.toString())

        val editStart = dialogLayout.findViewById<EditText>(R.id.set_start_position)
        editStart.setText((test.startPosition + 1).toString())

        val checkRandom = dialogLayout.findViewById<CheckBox>(R.id.check_random)
        checkRandom.isChecked = sharedPreferenceManager.random
        checkRandom.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.random = isChecked }

        val checkReverse = dialogLayout.findViewById<CheckBox>(R.id.check_reverse)
        checkReverse.isChecked = sharedPreferenceManager.reverse
        checkReverse.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.reverse = isChecked }

        val checkManual = dialogLayout.findViewById<CheckBox>(R.id.check_manual)
        checkManual.isChecked = sharedPreferenceManager.manual
        checkManual.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.manual = isChecked }

        val checkAudio = dialogLayout.findViewById<CheckBox>(R.id.check_audio)
        checkAudio.isChecked = sharedPreferenceManager.audio
        checkAudio.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.audio = isChecked }

        val checkRefine = dialogLayout.findViewById<CheckBox>(R.id.check_refine)
        checkRefine.isChecked = sharedPreferenceManager.refine
        checkRefine.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.refine = isChecked }

        val checkAlwaysReview = dialogLayout.findViewById<CheckBox>(R.id.check_always_review)
        checkAlwaysReview.isChecked = sharedPreferenceManager.alwaysReview
        checkAlwaysReview.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.alwaysReview = isChecked }

        val checkCaseInsensitive = dialogLayout.findViewById<CheckBox>(R.id.check_case_insensitive)
        checkCaseInsensitive.isChecked = sharedPreferenceManager.isCaseInsensitive
        checkCaseInsensitive.setOnCheckedChangeListener { _, isChecked -> sharedPreferenceManager.isCaseInsensitive = isChecked }
        if (Locale.getDefault().language != "en") checkCaseInsensitive.visibility = View.GONE

        val buttonStart = dialogLayout.findViewById<Button>(R.id.button_start)
        buttonStart.setOnClickListener {
            startAnswer(test, editStart.text.toString(), editLimit.text.toString())
        }

        val builder = AlertDialog.Builder(this@ShowTestsActivity, R.style.MyAlertDialogStyle)
        builder.setView(dialogLayout)
        builder.setTitle(getString(R.string.way))
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setNegativeButton(android.R.string.cancel, null)

        if (!sharedPreferenceManager.isShowPlaySettingDialog) {
            startAnswer(test, editStart.text.toString(), editLimit.text.toString())
        } else {
            val dialog = builder.show()
            hideDefaultButtonsFromDialog(dialog)

        }


    }

    private fun hideDefaultButtonsFromDialog(dialog: AlertDialog) {

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)

        if (positiveButton != null) positiveButton.visibility = View.GONE

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        if (negativeButton != null) negativeButton.visibility = View.GONE

    }

    private fun startAnswer(test: Test, start: String, limit: String) {

        var incorrect = false

        for (element in test.questions) if (!(element.isCorrect)) incorrect = true

        if (!incorrect && sharedPreferenceManager.refine) {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show()

        } else if (limit == "") {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_number), Toast.LENGTH_SHORT).show()

        } else if (start == "" || start.toInt() > test.questions.size || start.toInt() < 1) {

            Toast.makeText(this@ShowTestsActivity, getString(R.string.message_null_start), Toast.LENGTH_SHORT).show()

        } else {

            val result = test.copy(
                    limit = Integer.parseInt(limit),
                    startPosition = Integer.parseInt(start) - 1,
                    history = Calendar.getInstance().timeInMillis)

            testViewModel.update(result)

            if (sharedPreferenceManager.manual) {
                SelfJudgePlayActivity.startActivity(this@ShowTestsActivity, result.id)
            } else {
                NewPlayActivity.startActivity(this@ShowTestsActivity, result.id)
            }
        }
    }

    private fun login(test: RealmTest) {

        selectedTest = test

        AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    val providers = arrayListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setTosAndPrivacyPolicyUrls(
                                            "https://testmaker-1cb29.firebaseapp.com/terms",
                                            "https://testmaker-1cb29.firebaseapp.com/privacy")
                                    .build(),
                            REQUEST_SIGN_IN_UPLOAD)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_SIGN_IN_UPLOAD && resultCode == Activity.RESULT_OK) {
            selectedTest?.let {
                uploadTest(it)
                selectedTest = null
            }
        }

    }

    companion object {
        const val REQUEST_EDIT = 11111
        const val REQUEST_SIGN_IN_UPLOAD = 54321
    }

}
