package jp.gr.java_conf.foobar.testmaker.service.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import jp.gr.java_conf.foobar.testmaker.service.*
import jp.gr.java_conf.foobar.testmaker.service.BillingManager.BILLING_MANAGER_NOT_INITIALIZED
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.toTest
import jp.gr.java_conf.foobar.testmaker.service.models.CategoryEditor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*


class MainActivity : ShowTestsActivity(), BillingProvider {

    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var billingManager: BillingManager

    private lateinit var viewController: MainViewController

    private lateinit var binding: ActivityMainBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = MainViewModel()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.model = viewModel

        viewModel.title.observe(this, Observer {
            binding.buttonAdd.isEnabled = it?.isNotEmpty() ?: false
        })

        sendScreen("MainActivity")
        createAd(binding.container)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        viewController = MainViewController(this)
        billingManager = BillingManager(this, viewController.mUpdateListener)

        initNavigationView()

        initViews()

        initTestAndFolderAdapter(setValue = {
            testAndFolderAdapter.tests = realmController.nonCategorizedTests
            testAndFolderAdapter.categories = realmController.existingCateList
            testAndFolderAdapter.allTests = realmController.list
        })

        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = testAndFolderAdapter

    }

    private fun initViews() {

        binding.buttonUpload.setOnClickListener {

            if (binding.body.visibility == View.GONE) {
                binding.test.editTitle.requestFocus()
            }

            viewModel.isEditing.postValue(!(viewModel.isEditing.value ?: false))
        }

        binding.test.editTitle.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // ソフトキーボードを表示する
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                // ソフトキーボードを閉じる
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }// フォーカスが外れたとき
        }

        binding.test.buttonCategory.tag = ""
        binding.test.buttonCategory.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(binding.test.editTitle.windowToken, 0)
            val categoryEditor = CategoryEditor(this@MainActivity, binding.test.buttonCategory, realmController, testAndFolderAdapter)
            categoryEditor.setCategory()
        }

        if (Build.VERSION.SDK_INT >= 21) binding.test.buttonCategory.stateListAnimator = null

        binding.test.buttonCategory.setOnLongClickListener {

            val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
            builder.setMessage(getString(R.string.cancel_category))
            builder.setPositiveButton(android.R.string.ok) { _, _ ->

                binding.test.buttonCategory.tag = ""
                binding.test.buttonCategory.text = getString(R.string.category)
                binding.test.buttonCategory.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

            }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.create().show()

            false
        }

        if (Build.VERSION.SDK_INT >= 21) binding.buttonAdd.stateListAnimator = null

        binding.buttonAdd.setOnClickListener {

            realmController.addTest(binding.test.editTitle.text.toString(), binding.test.colorChooser.getColorId(), binding.test.buttonCategory.tag.toString())

            Toast.makeText(this@MainActivity, getString(R.string.message_add), Toast.LENGTH_LONG).show()

            testAndFolderAdapter.setValue()

            viewModel.isEditing.postValue(false)

            binding.test.editTitle.setText("")
            binding.test.buttonCategory.tag = ""
            binding.test.buttonCategory.text = getString(R.string.category)
            binding.test.buttonCategory.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

            sendEvent("createTest")
            sendFirebaseEvent("add-test")

        }

    }

    private fun initNavigationView() {

        setSupportActionBar(binding.toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_help //editProActivityにも同様の記述
                -> {

                    sendEvent("help")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse(getString(R.string.help_url))))

                }

                R.id.nav_review -> {

                    sendEvent("review")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://play.google.com/store/apps/details?id=jp.gr.java_conf.foobar.testmaker.service&amp;hl=ja")))
                }

                R.id.nav_others -> {

                    sendEvent("editOthers")
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse("http://play.google.com/store/apps/developer?id=banira")))
                }

                R.id.nav_import -> {

                    sendEvent("import")

                    if (Build.VERSION.SDK_INT <= 18) {
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "text/*"
                        startActivityForResult(intent, REQUEST_IMPORT)
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "text/*"
                        startActivityForResult(intent, REQUEST_IMPORT)
                    }
                }

                R.id.nav_license -> {

                    val licenseIntent = Intent(this@MainActivity, WebViewActivity::class.java)
                    licenseIntent.putExtra("url", "file:///android_asset/licenses.html")
                    startActivity(licenseIntent)
                }
                R.id.nav_paste -> {

                    sendEvent("paste")

                    val dialogLayout = layoutInflater.inflate(R.layout.dialog_paste, findViewById(R.id.layout_dialog_paste))

                    val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    builder.setView(dialogLayout)
                    builder.setTitle(getString(R.string.action_paste))
                    val dialog = builder.show()

                    val editPaste = dialogLayout.findViewById<EditText>(R.id.edit_paste)
                    val buttonImport = dialogLayout.findViewById<Button>(R.id.button_paste)

                    if (Build.VERSION.SDK_INT >= 21) buttonImport.stateListAnimator = null

                    buttonImport.setOnClickListener { _ ->

                        loadTestByText(editPaste.text.toString())

                        dialog.dismiss()
                    }
                }

                R.id.nav_online -> {

                    sendEvent("online")
                    startActivityForResult(Intent(this@MainActivity, OnlineMainActivity::class.java), REQUEST_EDIT)
                }

                R.id.nav_move_questions -> {

                    sendEvent("move questions")
                    startActivityForResult(Intent(this@MainActivity, MoveQuestionsActivity::class.java), REQUEST_EDIT)

                }

                R.id.nav_study_plus -> {

                    sendEvent("study plus")
                    startActivityForResult(Intent(this@MainActivity, StudyPlusActivity::class.java), REQUEST_EDIT)

                }

                R.id.nav_remove_ad -> {

                    sendEvent("remove ad")

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
            testAndFolderAdapter.setValue()
            binding.drawerLayout.closeDrawers()
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
                loadTestByText(it.bufferedReader().use(BufferedReader::readText))
            }

        } catch (e: FileNotFoundException) {
            throw RuntimeException(e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtil.forceClose(inputStream)
        }
    }

    private fun loadTestByText(text: String) {

        GlobalScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.Default) { text.toTest(baseContext) }.let {
                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, it.title), Toast.LENGTH_LONG).show()
                realmController.convert(it, -1)
                testAndFolderAdapter.setValue()

            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
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
        binding.container.visibility = View.GONE
    }

    companion object {
        const val REQUEST_IMPORT = 12345
    }
}
