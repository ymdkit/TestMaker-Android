package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.billingclient.api.BillingClient
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.move.MoveQuestionsActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.preference.SettingsActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.*
import java.util.*


class MainActivity : BaseActivity() {

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()
    private val service: CloudFunctionsService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.offscreenPageLimit = 1

        binding.viewPager.adapter = ViewPagerAdapter(this, listOf(
                LocalMainFragment(),
                AccountMainFragment(object : AccountMainFragment.OnTestDownloadedListener {
                    override fun onDownloaded() {
                        binding.viewPager.setCurrentItem(PAGE_LOCAL, true)
                    }

                })))

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = listOf(getString(R.string.tab_local), getString(R.string.tab_remote))[position]
            tab.icon = listOf(ResourcesCompat.getDrawable(resources, R.drawable.ic_device_24, null),
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_account, null))[position]
        }.attach()

        createAd(binding.adView)
        initNavigationView()

        viewModel.startConnection()
        viewModel.billingStatus.observeNonNull(this) {
            when (it) {
                is BillingStatus.Error -> {
                    when (it.responseCode) {
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            Toast.makeText(baseContext, getString(R.string.alrady_removed_ad), Toast.LENGTH_SHORT).show()
                            binding.adView.visibility = View.GONE
                            viewModel.removeAd()
                        }
                        BillingClient.BillingResponseCode.USER_CANCELED -> Toast.makeText(baseContext, getString(R.string.purchase_canceled), Toast.LENGTH_SHORT).show()
                        else -> Toast.makeText(baseContext, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
                is BillingStatus.PurchaseSuccess -> {

                    it.purchases?.let {
                        for (purchase in it) {
                            when (purchase.sku) {
                                getString(R.string.sku_remove_ad) -> {
                                    binding.adView.visibility = View.GONE
                                    viewModel.removeAd()
                                }
                            }
                        }
                    }
                }
                else -> {
                }
            }
        }

        lifecycleScope.launch {
            val pendingDynamicLinkData = withContext(Dispatchers.Default) {
                FirebaseDynamicLinks.getInstance()
                        .getDynamicLink(intent).await()
            }

            pendingDynamicLinkData ?: return@launch

            val dialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.downloading))
                    .setView(LayoutInflater.from(this@MainActivity).inflate(R.layout.dialog_progress, findViewById(R.id.layout_progress))).show()

            val deepLink = pendingDynamicLinkData.link

            when (val result = viewModel.downloadTest(deepLink.toString().split("/").last())) {
                is FirebaseTestResult.Success -> {
                    viewModel.convert(result.test)
                    testViewModel.refresh()
                    showToast(getString(R.string.msg_success_download_test, result.test.name))
                }
                is FirebaseTestResult.Failure -> {
                    showToast(result.message)
                }
            }
            dialog.dismiss()

        }

    }

    private fun initNavigationView() {

        setSupportActionBar(binding.toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_help //editProActivityにも同様の記述
                -> {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri
                            .parse(getString(R.string.help_url))))
                }
                R.id.nav_feedback
                -> {
                    val addresses = arrayOf<String>(getString(R.string.contact_email))
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, addresses)
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject_feedback))
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.email_body_feedback, Build.VERSION.SDK_INT))
                    }
                    if (intent.resolveActivity(packageManager) != null) {
                        startActivity(intent)
                    }
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

                        loadTestByText(text = editPaste.text.toString())

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

                    viewModel.purchaseRemoveAd(this, BillingItem(getString(R.string.sku_remove_ad), BillingClient.SkuType.INAPP))
                }
            }
            false
        }

        drawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout,
                binding.toolbar, R.string.add,
                R.string.add)

        binding.drawerLayout.addDrawerListener(drawerToggle)

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

                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.let { cur ->
                    val nameIndex = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    cur.moveToFirst()
                    val title = cur.getString(nameIndex)
                    loadTestByText(title = title, text = text)
                    cur.close()
                }
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

    private fun loadTestByText(title: String = "", text: String) {
        lifecycleScope.launch {
            showProgress()
            runCatching {
                withContext(Dispatchers.IO) {
                    service.textToTest(title, text.replace("\n", "¥n").replace("<", "&lt;"), if (Locale.getDefault().language == "ja") "ja" else "en")
                }
            }.onSuccess {
                testViewModel.create(it)
                Toast.makeText(baseContext, baseContext.getString(R.string.message_success_load, it.title), Toast.LENGTH_LONG).show()
            }.onFailure {
                showErrorToast(it)
            }
            hideProgress()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    private inner class ViewPagerAdapter(activity: FragmentActivity, private val fragments: List<Fragment>) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    companion object {
        const val PAGE_LOCAL = 0
        const val PAGE_REMOTE = 1

        const val REQUEST_EDIT = 11111
        const val REQUEST_IMPORT = 12345
        const val REQUEST_SIGN_IN = 12346

        fun startActivityWithClear(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java).apply {

            }
            activity.startActivity(intent)
        }
    }
}
