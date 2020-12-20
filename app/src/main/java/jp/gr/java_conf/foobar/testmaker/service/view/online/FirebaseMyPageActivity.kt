package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMyPageBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.android.synthetic.main.activity_my_page.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class FirebaseMyPageActivity : BaseActivity(), FirebaseMyPageController.OnClickListener {

    private val viewModel: FirebaseMyPageViewModel by viewModel()

    private lateinit var controller: FirebaseMyPageController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_page)

        val binding = DataBindingUtil.setContentView<ActivityMyPageBinding>(this, R.layout.activity_my_page)
        createAd(binding.adView)

        initToolBar()

        swipe_refresh.isRefreshing = true

        controller = FirebaseMyPageController(this)
        controller.setOnClickListener(this)

        recycler_view.adapter = controller.adapter

        viewModel.getMyTests().observeNonNull(this) {

            controller.tests = it
            swipe_refresh.isRefreshing = false
        }

        edit_profile.setOnClickListener {

            val user = viewModel.getUser() ?: return@setOnClickListener
            val dialogLayout = LayoutInflater.from(this@FirebaseMyPageActivity).inflate(R.layout.dialog_edit_user_name, findViewById(R.id.layout_dialog_edit_user))
            val editUsername = dialogLayout.findViewById<EditText>(R.id.edit_user_name)
            editUsername.setText(user.displayName)
            val buttonSaveProfile = dialogLayout.findViewById<Button>(R.id.button_save_profile)
            val builder = AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
            builder.setView(dialogLayout)
            builder.setTitle(getString(R.string.message_edit_profile))
            val dialog = builder.show()

            buttonSaveProfile.setOnClickListener {

                if (editUsername.text.toString().isEmpty()) {
                    Toast.makeText(baseContext, getString(R.string.message_shortage), Toast.LENGTH_SHORT).show()

                } else {
                    viewModel.updateProfile(editUsername.text.toString()) { reloadUserProfile() }

                    dialog.dismiss()
                }
            }
        }

        swipe_refresh.setOnRefreshListener {
            viewModel.fetchMyTests()
        }

        reloadUserProfile()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyTests()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_firebase_my_page, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {

                AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.logout))
                        .setMessage(getString(R.string.msg_logout))
                        .setPositiveButton(getString(R.string.ok)) { _, _ ->
                            viewModel.logOut()
                            MainActivity.startActivityWithClear(this)
                        }
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show()
            }
            android.R.id.home -> {

                finish()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun reloadUserProfile() {

        val user = viewModel.getUser() ?: return
        text_user_name.text = getString(R.string.creator_name, user.displayName)

    }

    override fun onClickDownloadTest(document: DocumentSnapshot) {
        lifecycleScope.launch {

            val dialog = AlertDialog.Builder(this@FirebaseMyPageActivity)
                    .setTitle(getString(R.string.downloading))
                    .setView(LayoutInflater.from(this@FirebaseMyPageActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

            when (val result = viewModel.downloadTest(document.id)) {
                is FirebaseTestResult.Success -> {
                    viewModel.convert(result.test)

                    Toast.makeText(this@FirebaseMyPageActivity, getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@FirebaseMyPageActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                is FirebaseTestResult.Failure -> {
                    Toast.makeText(this@FirebaseMyPageActivity, result.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()

        }
    }

    override fun onClickDetailTest(document: DocumentSnapshot) {
        val data = document.toObject(FirebaseTest::class.java) ?: return

        val dialogLayout = LayoutInflater.from(this@FirebaseMyPageActivity).inflate(R.layout.dialog_online_test_info, findViewById(R.id.layout_dialog_info))

        val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
        textInfo.text = getString(R.string.info_firebase_test, data.userName, data.getDate(), data.overview)

        AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
                .setView(dialogLayout)
                .setTitle(data.name)
                .show()
    }

    override fun onClickDeleteTest(document: DocumentSnapshot) {
        AlertDialog.Builder(this@FirebaseMyPageActivity, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.delete_exam))
                .setMessage(getString(R.string.message_delete_exam, document.toObject(FirebaseTest::class.java)?.name))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    viewModel.deleteTest(document.id)
                    swipe_refresh.isRefreshing = true
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create().show()
    }
}
