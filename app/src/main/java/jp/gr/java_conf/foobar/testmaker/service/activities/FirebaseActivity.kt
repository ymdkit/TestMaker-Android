package jp.gr.java_conf.foobar.testmaker.service.activities

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
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.models.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.FirebaseTestAdapter
import kotlinx.android.synthetic.main.activity_online_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel




class FirebaseActivity : BaseActivity() {

    private val viewModel: FirebaseViewModel by viewModel()

    private lateinit var adapter: FirebaseTestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_main)
        swipe_refresh.isRefreshing = true

        createAd(container)

        initToolBar()

        adapter = FirebaseTestAdapter(baseContext)
        adapter.download = { data: DocumentSnapshot ->
            viewModel.downloadTest(data.id)
        }
        adapter.showInfo = { data: FirebaseTest ->
            val dialogLayout = LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

            val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
            textInfo.text = getString(R.string.info_firebase_test, data.userName, data.getDate(), data.overview)

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
            builder.setTitle(data.name)
            builder.show()
        }

        viewModel.getTests().observeNonNull(this) {
            recycler_view.visibility = View.VISIBLE
            swipe_refresh.isRefreshing = false

            recycler_view.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
            recycler_view.setHasFixedSize(true)
            recycler_view.adapter = this.adapter
            adapter.array = it
        }

        viewModel.getDownloadTest().observeNonNull(this) {

            viewModel.convert(it)
            finish()

        }

        swipe_refresh.setOnRefreshListener {
            viewModel.fetchTests()
        }

        button_upload.setOnClickListener {

            FirebaseAuth.getInstance().currentUser?.let {

                if (realmController.list.size < 1 || realmController.list.all { it.getQuestionsForEach().size < 1 }) {

                    Toast.makeText(baseContext, getString(R.string.message_non_exist_test), Toast.LENGTH_SHORT).show()

                    return@setOnClickListener
                }

                showDialogUpload()

            } ?: run {

                login()
            }
        }

        if(!sharedPreferenceManager.firebaseNotes){
            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setTitle(getString(R.string.notes))
                    .setMessage(getString(R.string.msg_notice_firebase))
                    .setPositiveButton(getString(R.string.show_never)) { _, _ ->
                        sharedPreferenceManager.firebaseNotes = true
                    }
                    .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_firebase, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            R.id.action_profile -> {

                FirebaseAuth.getInstance().currentUser?.let {

                    startActivityForResult(Intent(this@FirebaseActivity, FirebaseMyPageActivity::class.java), 0)

                } ?: run {

                    login()
                }
            }

            R.id.old -> {

                startActivityForResult(Intent(this@FirebaseActivity, OnlineMainActivity::class.java), 0)

            }

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
                val user = FirebaseAuth.getInstance().currentUser
                viewModel.createUser(user)

                Toast.makeText(this, "login success!", Toast.LENGTH_SHORT).show()
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

        AlertDialog.Builder(this)
                .setTitle(getString(R.string.login))
                .setMessage(getString(R.string.msg_not_login))
                .setPositiveButton(getString(R.string.ok)) { dialog, which ->
                    val providers = arrayListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build())

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .setTosAndPrivacyPolicyUrls(
                                            "https://keita-developer.hatenablog.com/entry/2019/07/01/103627",
                                            "https://keita-developer.hatenablog.com/entry/2019/07/01/103627")
                                    .build(),
                            RC_SIGN_IN)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
    }

    private fun showDialogUpload() {

        var position = 0

        val dialogLayout = LayoutInflater.from(this@FirebaseActivity).inflate(R.layout.dialog_upload, findViewById(R.id.layout_dialog_upload))

        val tests = realmController.listNotEmpty

        val array = Array(tests.size) { i -> tests[i].title }

        val spinner = dialogLayout.findViewById<Spinner>(R.id.spinner)
        val editOverView = dialogLayout.findViewById<EditText>(R.id.edit_overview)
        // ArrayAdapter
        val adapter = ArrayAdapter(baseContext,
                android.R.layout.simple_spinner_item, array)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // spinner に adapter をセット
        spinner.adapter = adapter

        // リスナーを登録
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, positionSpinner: Int, id: Long) {
                position = positionSpinner
            }

            //　アイテムが選択されなかった
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val builder = AlertDialog.Builder(this@FirebaseActivity, R.style.MyAlertDialogStyle)
        builder.setView(dialogLayout)
        builder.setTitle(getString(R.string.message_upload_test))
        builder.setPositiveButton(android.R.string.ok, null)

        builder.setNegativeButton(android.R.string.cancel, null)
        val dialog = builder.show()

        val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.setOnClickListener {

            viewModel.uploadTest(tests[position], editOverView.text.toString(), success = {
                Toast.makeText(this, "upload success!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                viewModel.fetchTests()
            })

        }

    }

    companion object {
        const val RC_SIGN_IN = 54321
    }

}
