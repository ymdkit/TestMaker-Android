package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.billingclient.api.BillingClient
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMainBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.infra.util.TestMakerFileReader
import jp.gr.java_conf.foobar.testmaker.service.view.group.GroupActivity
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


class MainActivity : BaseActivity(), AccountMainFragment.OnTestDownloadedListener {

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()
    private val service: CloudFunctionsService by inject()
    private val logger: TestMakerLogger by inject()

    private val importFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        val (title, content) = TestMakerFileReader.readFileFromUri(it, this)
        loadTestByText(title = title, text = content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.adapter = ViewPagerAdapter(
            this, listOf(
                LocalMainFragment(),
                AccountMainFragment()
            )
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text =
                listOf(getString(R.string.tab_local), getString(R.string.tab_remote))[position]
            tab.icon = listOf(
                ResourcesCompat.getDrawable(resources, R.drawable.ic_device_24, null),
                ResourcesCompat.getDrawable(resources, R.drawable.ic_account, null)
            )[position]
        }.attach()

        createAd(binding.adView)
        initNavigationView()

        viewModel.startBillingConnection()
        viewModel.billingStatus.observeNonNull(this) {
            when (it) {
                is BillingStatus.Error -> {
                    when (it.responseCode) {
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            Toast.makeText(
                                baseContext,
                                getString(R.string.alrady_removed_ad),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.adView.visibility = View.GONE
                            viewModel.removeAd()
                        }
                        BillingClient.BillingResponseCode.USER_CANCELED -> Toast.makeText(
                            baseContext,
                            getString(R.string.purchase_canceled),
                            Toast.LENGTH_SHORT
                        ).show()
                        else -> Toast.makeText(
                            baseContext,
                            getString(R.string.error),
                            Toast.LENGTH_SHORT
                        ).show()
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
            val deepLink = pendingDynamicLinkData.link
            handleDynamicLink(deepLink.toString())
        }
    }

    private fun handleDynamicLink(link: String) {

        val regex = Regex("""(?<=https://testmaker-1cb29\.com/).*|(?<=https://ankimaker\.com/).*""")
        val result = regex.find(link, 0)

        result?.value?.let {

            val params = it.split("/")

            if (params.first() == "groups") {

                if (params.size != 2) return@let

                val groupId = params[1]
                GroupActivity.startActivityWithGroupId(this, groupId)

            } else {
                actionDownload(params[0])
            }
        }
    }

    private fun actionDownload(documentId: String) = lifecycleScope.launch {

        executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                viewModel.downloadTest(documentId)
            },
            onSuccess = {
                viewModel.convert(it)
                testViewModel.refresh()
                showToast(getString(R.string.msg_success_download_test, it.name))
                logger.logCreateTestEvent(it.name, CreateTestSource.DYNAMIC_LINKS.title)
            },
            onFailure = {
                showToast(getString(R.string.msg_failure_download_test))
            }
        )
    }


    private fun initNavigationView() {

        setSupportActionBar(binding.toolbar)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_help
                -> {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(getString(R.string.help_url))
                        )
                    )
                }
                R.id.nav_feedback
                -> {
                    val emailIntent = Intent(Intent.ACTION_SENDTO)
                    emailIntent.data = Uri.parse("mailto:")
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
                    emailIntent.putExtra(
                        Intent.EXTRA_SUBJECT,
                        getString(R.string.email_subject_feedback)
                    )
                    emailIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.email_body_feedback, Build.VERSION.SDK_INT)
                    )
                    startActivity(Intent.createChooser(emailIntent, null))

                }
                R.id.nav_import -> importFile.launch(arrayOf("text/*"))
                R.id.nav_paste -> {

                    val dialogLayout = layoutInflater.inflate(
                        R.layout.dialog_paste,
                        findViewById(R.id.layout_dialog_paste)
                    )

                    val builder = AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    builder.setView(dialogLayout)
                    builder.setTitle(getString(R.string.action_paste))
                    val dialog = builder.show()

                    val editPaste = dialogLayout.findViewById<EditText>(R.id.edit_paste)
                    val buttonImport = dialogLayout.findViewById<Button>(R.id.button_paste)

                    buttonImport.setOnClickListener {
                        logger.logEvent("paste_import")
                        loadTestByText(text = editPaste.text.toString())
                        dialog.dismiss()
                    }
                }
                R.id.nav_settings -> startActivity(
                    Intent(
                        this@MainActivity,
                        SettingsActivity::class.java
                    )
                )
                R.id.nav_online -> startActivity(
                    Intent(
                        this@MainActivity,
                        FirebaseActivity::class.java
                    )
                )
                R.id.nav_group -> startActivity(
                    Intent(
                        this@MainActivity,
                        GroupActivity::class.java
                    )
                )
                R.id.nav_move_questions -> startActivity(
                    Intent(
                        this@MainActivity,
                        MoveQuestionsActivity::class.java
                    )
                )
                R.id.nav_remove_ad -> {
                    viewModel.purchaseRemoveAd(
                        this,
                        BillingItem(getString(R.string.sku_remove_ad), BillingClient.SkuType.INAPP)
                    )
                }
            }
            false
        }

        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout,
            binding.toolbar, R.string.add,
            R.string.add
        )

        binding.drawerLayout.addDrawerListener(drawerToggle)

    }

    private fun loadTestByText(title: String = "", text: String) {

        executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                withContext(Dispatchers.IO) {
                    service.textToTest(
                        title,
                        text.replace("\n", "¥n").replace("<", "&lt;"),
                        if (Locale.getDefault().language == "ja") "ja" else "en"
                    )
                }
            },
            onSuccess = {
                testViewModel.create(Test.createFromTestResponse(it))
                logger.logCreateTestEvent(it.title, CreateTestSource.FILE_IMPORT.title)
                Toast.makeText(
                    baseContext,
                    baseContext.getString(R.string.message_success_load, it.title),
                    Toast.LENGTH_LONG
                ).show()
            },
            onFailure = {
                showErrorToast(it)
            }
        )
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    private inner class ViewPagerAdapter(
        activity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }

    companion object {
        const val PAGE_LOCAL = 0
        const val PAGE_REMOTE = 1

        const val REQUEST_SIGN_IN = 12346

        fun startActivityWithClear(activity: Activity) {
            val intent = Intent(activity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            activity.startActivity(intent)
        }
    }

    override fun onDownloaded() {
        binding.viewPager.setCurrentItem(PAGE_LOCAL, true)
    }
}
