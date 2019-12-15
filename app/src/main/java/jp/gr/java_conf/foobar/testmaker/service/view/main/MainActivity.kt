package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.android.billingclient.api.BillingClient
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTest
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingManager
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingManager.BILLING_MANAGER_NOT_INITIALIZED
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingProvider
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.category.CategoryEditor
import jp.gr.java_conf.foobar.testmaker.service.view.move.MoveQuestionsActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageActivity
import jp.gr.java_conf.foobar.testmaker.service.view.preference.SettingsActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ShowTestsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*


class MainActivity : ShowTestsActivity(), BillingProvider {

    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var billingManager: BillingManager

    private lateinit var viewController: MainViewController

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.model = viewModel

        viewModel.title.observe(this, Observer {
            binding.buttonAdd.isEnabled = it?.isNotEmpty() ?: false
        })

        createAd(binding.adView)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewController = MainViewController(this)
        billingManager = BillingManager(this, viewController.mUpdateListener)

        initNavigationView()

        initViews()

        initTestAndFolderAdapter()

        viewModel.getExistingCategoryList().observeNonNull(this) {
            mainController.categories = it
        }

        viewModel.getTests().observeNonNull(this) {
            mainController.tests = it
        }

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = mainController.adapter


        GlobalScope.launch(Dispatchers.Default) {
            val pendingDynamicLinkData = FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(intent).await() ?: return@launch

            val deepLink = pendingDynamicLinkData.link

            withContext(Dispatchers.Main) {
                val dialog = AlertDialog.Builder(this@MainActivity)
                        .setTitle(getString(R.string.downloading))
                        .setView(LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

                when (val result = viewModel.downloadTest(deepLink.toString().split("/").last())) {
                    is FirebaseTestResult.Success -> {
                        viewModel.convert(result.test)
                        Toast.makeText(this@MainActivity, getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                    }
                    is FirebaseTestResult.Failure -> {
                        Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
        }
    }

    private fun initViews() {

        binding.buttonExpand.setOnClickListener {

            if (viewModel.isEditing.value != true) {
                binding.test.editTitle.requestFocus()
            }

            viewModel.isEditing.postValue(!(viewModel.isEditing.value ?: false))
        }

        binding.test.editTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }

        binding.test.buttonCategory.tag = ""
        binding.test.buttonCategory.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(binding.test.editTitle.windowToken, 0)
            val categoryEditor = CategoryEditor(this@MainActivity,
                    binding.test.buttonCategory,
                    getCategories = { viewModel.getCategories() }
                    ,
                    addCategory = {
                        viewModel.addCategory(it)
                    },
                    deleteCategory = {
                        viewModel.deleteCategory(it)
                    })
            categoryEditor.setCategory()
        }

        binding.test.buttonCategory.setOnLongClickListener {

            AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
                    .setMessage(getString(R.string.cancel_category))
                    .setPositiveButton(android.R.string.ok) { _, _ ->

                        binding.test.buttonCategory.tag = ""
                        binding.test.buttonCategory.text = getString(R.string.category)
                        binding.test.buttonCategory.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show()

            false
        }

        binding.buttonAdd.setOnClickListener {

            viewModel.addTest(binding.test.editTitle.text.toString(), binding.test.colorChooser.getColorId(), binding.test.buttonCategory.tag.toString())

            Toast.makeText(this@MainActivity, getString(R.string.message_add), Toast.LENGTH_LONG).show()

            viewModel.isEditing.postValue(false)

            binding.test.editTitle.setText("")
            binding.test.buttonCategory.tag = ""
            binding.test.buttonCategory.text = getString(R.string.category)
            binding.test.buttonCategory.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

            sendFirebaseEvent("add-test")

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.itemId == R.id.action_compare) {

            AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setTitle(getString(R.string.sort))
                    .setItems(resources.getStringArray(R.array.sort_exam)) { _, which ->

                        sharedPreferenceManager.sort = which
                        viewModel.fetchTests()

                    }.show()

        }

        return super.onOptionsItemSelected(item)
    }



    private fun initNavigationView() {

        setSupportActionBar(binding.toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_my_page -> {

                    if (viewModel.getUser() != null) {
                        startActivityForResult(Intent(this@MainActivity, FirebaseMyPageActivity::class.java), 0)
                    } else {
                        AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                                .setTitle(getString(R.string.login))
                                .setMessage(getString(R.string.msg_not_login))
                                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                                    startActivityForResult(
                                            viewModel.getAuthUIIntent(),
                                            REQUEST_SIGN_IN)
                                }
                                .setNegativeButton(getString(R.string.cancel), null)
                                .show()
                    }
                }
                R.id.nav_help //editProActivityにも同様の記述
                -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse(getString(R.string.help_url))))
                }
                R.id.nav_review -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://play.google.com/store/apps/details?id=jp.gr.java_conf.foobar.testmaker.service&amp;hl=ja")))
                }
                R.id.nav_import -> {

                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.type = "text/*"
                    startActivityForResult(intent, REQUEST_IMPORT)

                }
                R.id.nav_paste -> {

                    val dialogLayout = layoutInflater.inflate(R.layout.dialog_paste, findViewById(R.id.layout_dialog_paste))

                    val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    builder.setView(dialogLayout)
                    builder.setTitle(getString(R.string.action_paste))
                    val dialog = builder.show()

                    val editPaste = dialogLayout.findViewById<EditText>(R.id.edit_paste)
                    val buttonImport = dialogLayout.findViewById<Button>(R.id.button_paste)

                    buttonImport.setOnClickListener {

                        loadTestByText(editPaste.text.toString())

                        dialog.dismiss()
                    }
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                }

                R.id.nav_online -> {
                    startActivityForResult(Intent(this@MainActivity, FirebaseActivity::class.java), REQUEST_EDIT)
                }
                R.id.nav_move_questions -> {
                    startActivityForResult(Intent(this@MainActivity, MoveQuestionsActivity::class.java), REQUEST_EDIT)
                }
                R.id.nav_remove_ad -> {
                    if (billingManager.billingClientResponseCode <= BILLING_MANAGER_NOT_INITIALIZED) return@setNavigationItemSelectedListener false
                    if (isFinishing) return@setNavigationItemSelectedListener false

                    getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP, listOf("removead")
                    ) { responseCode, skuDetailsList ->
                        if (responseCode != BillingClient.BillingResponse.OK) {

                            Toast.makeText(baseContext, getString(R.string.error), Toast.LENGTH_SHORT).show()

                        } else if (skuDetailsList != null && skuDetailsList.size > 0) {
                            // If we successfully got SKUs, add a header in front of the row
                            // Then fill all the other rows
                            for (details in skuDetailsList) {

                                if (isPremiumPurchased) {

                                    Toast.makeText(baseContext, getString(R.string.alrady_removed_ad), Toast.LENGTH_SHORT).show()

                                } else {

                                    getBillingManager().initiatePurchaseFlow(details.sku,
                                            BillingClient.SkuType.INAPP)
                                }
                            }

                        } else {
                            // Handle empty state
                            Toast.makeText(baseContext, getString(R.string.error), Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            }
            false
        }

        drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout,
                binding.toolbar, R.string.add,
                R.string.add)

        binding.drawerLayout.addDrawerListener(drawerToggle)

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        super.onBackPressed()
    }

    override fun onPause() {
        inputMethodManager.hideSoftInputFromWindow(binding.test.editTitle.windowToken, 0)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            binding.drawerLayout.closeDrawers()
        }

        if (requestCode == REQUEST_SIGN_IN) {
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

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == REQUEST_IMPORT) {

            data?.also {
                launchEditorActivity(it.data)
            }
        }
    }

    private fun launchEditorActivity(uri: Uri?) {

        if (uri == null) return
        var inputStream: InputStream? = null

        try {

            inputStream = contentResolver.openInputStream(uri)
            inputStream?.also {

                var text = it.bufferedReader().use(BufferedReader::readText)

                if (text[0].toString() == "\uFEFF") {
                    text = text.substring(1)
                }

                loadTestByText(text)
            }

        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
        }
    }

    private fun loadTestByText(text: String) {

        val questionId = viewModel.getMaxQuestionId()

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { text.toTest(baseContext, questionId) }.let {
                viewModel.addOrUpdateTest(it)
                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, it.title), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTests()
        viewModel.fetchCategories()
    }

    public override fun onDestroy() {
        billingManager.destroy()
        super.onDestroy()
    }

    override fun getBillingManager(): BillingManager {
        return billingManager
    }

    override fun isPremiumPurchased(): Boolean {
        return sharedPreferenceManager.isRemovedAd
    }

    fun removeAd() {
        binding.adView.visibility = View.GONE
    }

    companion object {
        const val REQUEST_IMPORT = 12345
        const val REQUEST_SIGN_IN = 12346
    }
}
