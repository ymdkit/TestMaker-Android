package jp.gr.java_conf.foobar.testmaker.service.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import jp.gr.java_conf.foobar.testmaker.service.*
import jp.gr.java_conf.foobar.testmaker.service.BillingManager.BILLING_MANAGER_NOT_INITIALIZED
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncTaskLoadTest
import jp.gr.java_conf.foobar.testmaker.service.models.CategoryEditor
import jp.gr.java_conf.foobar.testmaker.service.models.StructTest
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.FolderAdapter
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.MyScrambleAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_edit_test.*
import java.io.*


class MainActivity : ShowTestsActivity(), BillingProvider {

    override fun getBillingManager(): BillingManager {
        return billingManager
    }

    override fun isPremiumPurchased(): Boolean {
        return sharedPreferenceManager.isRemovedAd
    }

    private lateinit var inputMethodManager: InputMethodManager

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var billingManager: BillingManager

    private lateinit var viewController: MainViewController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        sendScreen("MainActivity")

        createAd(container)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        viewController = MainViewController(this)

        billingManager = BillingManager(this, viewController.mUpdateListener)

        initNavigationView()

        initViews()

        initTestAdapter()

        val folderAdapter = FolderAdapter(this, realmController)

        parentAdapter = MyScrambleAdapter(this, realmController.mixedList, null, realmController,
                testAdapter,
                folderAdapter
        )

        folderAdapter.setOnClickListener(object : FolderAdapter.OnClickListener {
            override fun onClick(category: String) {

                val i = Intent(this@MainActivity, CategorizedActivity::class.java)
                i.putExtra("category", category)

                startActivityForResult(i, REQUEST_EDIT)

            }

        })

        recycler_view.layoutManager = LinearLayoutManager(applicationContext)
        recycler_view.setHasFixedSize(true) // アイテムは固定サイズ
        recycler_view.adapter = parentAdapter

    }

    private fun initViews() {

        button_upload.setOnClickListener {

            if (body.visibility != View.GONE) {
                body.visibility = View.GONE
                button_upload.setImageResource(R.drawable.ic_expand_more_black)
            } else {
                body.visibility = View.VISIBLE
                button_upload.setImageResource(R.drawable.ic_expand_less_black)
                edit_title.isFocusable = true
                edit_title.requestFocus()
            }
        }

        edit_title.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                // ソフトキーボードを表示する
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                // ソフトキーボードを閉じる
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }// フォーカスが外れたとき
        }

        button_category.tag = ""
        button_category.setOnClickListener {
            inputMethodManager.hideSoftInputFromWindow(edit_title.windowToken, 0)
            val categoryEditor = CategoryEditor(this@MainActivity, button_category, realmController, parentAdapter)
            categoryEditor.setCategory()
        }

        if (Build.VERSION.SDK_INT >= 21) button_category.stateListAnimator = null

        button_category.setOnLongClickListener {

            val builder = AlertDialog.Builder(this@MainActivity, R.style.MyAlertDialogStyle)
            builder.setMessage(getString(R.string.cancel_category))
            builder.setPositiveButton(android.R.string.ok) { _, _ ->

                button_category.tag = ""
                button_category.text = getString(R.string.category)
                button_category.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

            }

            builder.setNegativeButton(android.R.string.cancel, null)
            builder.create().show()

            false
        }

        if (Build.VERSION.SDK_INT >= 21) button_add.stateListAnimator = null

        button_add.setOnClickListener {

            if (edit_title.text.toString() == "") {

                Toast.makeText(this@MainActivity, getString(R.string.message_wrong), Toast.LENGTH_LONG).show()

            } else {

                realmController.addTest(edit_title.text.toString(), color_chooser.getColorId(), button_category.tag.toString())

                Toast.makeText(this@MainActivity, getString(R.string.message_add), Toast.LENGTH_LONG).show()

                parentAdapter?.notifyDataSetChanged()

                edit_title.setText("")
                button_category.tag = ""
                button_category.text = getString(R.string.category)

                button_category.background = ResourcesCompat.getDrawable(resources, R.drawable.button_blue, null)

                body.visibility = View.GONE
                button_upload.setImageResource(R.drawable.ic_expand_more_black)

                sendEvent("createTest")
                sendFirebaseEvent("add-test")

            }

        }

    }

    private fun initNavigationView() {

        setSupportActionBar(toolbar)

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
                        //APIレベル18以前の機種の場合の処理
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "text/*"
                        startActivityForResult(intent, 12346)
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        //APIレベル19以降の機種の場合の処理
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.type = "text/*"
                        startActivityForResult(intent, 12345)

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

                        loadText(editPaste.text.toString())

                        dialog.dismiss()
                    }
                }

                R.id.nav_online -> {

                    sendEvent("online")

                    val licenseIntent = Intent(this@MainActivity, OnlineMainActivity::class.java)
                    licenseIntent.putExtra("url", "file:///android_asset/licenses.html")
                    startActivityForResult(licenseIntent, REQUEST_EDIT)
                }

                R.id.nav_move_questions -> {

                    sendEvent("move questions")

                    val intent = Intent(this@MainActivity, MoveQuestionsActivity::class.java)
                    startActivityForResult(intent, REQUEST_EDIT)

                }

                R.id.nav_study_plus -> {

                    sendEvent("study plus")

                    val intent = Intent(this@MainActivity, StudyPlusActivity::class.java)
                    startActivityForResult(intent, REQUEST_EDIT)

                }

                R.id.nav_remove_ad -> {

                    sendEvent("remove ad")

                    if (billingManager.billingClientResponseCode > BILLING_MANAGER_NOT_INITIALIZED) {

                        if (!isFinishing) {

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

                }
            }
            false
        }

        drawerToggle = ActionBarDrawerToggle(this, drawer_layout,
                toolbar, R.string.add,
                R.string.add)

        drawer_layout.addDrawerListener(drawerToggle)

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        super.onBackPressed()
    }

    override fun onPause() {
        inputMethodManager.hideSoftInputFromWindow(edit_title.windowToken, 0)

        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == Activity.RESULT_CANCELED) {
            parentAdapter?.notifyDataSetChanged()

            drawer_layout.closeDrawers()
        }

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == 12345) {

            launchEditorActivity(data!!.data)

        } else if (requestCode == 12346) {

            launchEditorActivity(data!!.data)

        }
    }

    private fun launchEditorActivity(uri: Uri?) {

        if (uri == null) return

        var inputStream: InputStream? = null

        try {

            inputStream = contentResolver.openInputStream(uri)

            loadText(inputStream.bufferedReader().use(BufferedReader::readText))

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

    private fun loadText(text: String) {

        val loader = AsyncTaskLoadTest(text, baseContext)
        loader.setCallback(object : AsyncTaskLoadTest.AsyncTaskCallback {
            override fun postExecute(result: StructTest) {

                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, result.title), Toast.LENGTH_LONG).show()

                realmController.convert(result, -1)

                parentAdapter?.notifyDataSetChanged()

            }

            override fun progressUpdate(progress: Int) {
            }

            override fun cancel() {
            }

            override fun preExecute() {
            }

        })

        loader.execute()

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()

    }

    public override fun onDestroy() {

        billingManager.destroy()

        super.onDestroy()
    }

    fun removeAd() {
        container.visibility = View.GONE
    }
}
