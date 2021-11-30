package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResultListener
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.billingclient.api.BillingClient
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentHomeBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.infra.api.CloudFunctionsService
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.infra.util.TestMakerFileReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_WORKBOOK_CREATED = "request_workbook_created"
    }

    private lateinit var binding: FragmentHomeBinding

    val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val viewModel: MainViewModel by viewModel()
    private val service: CloudFunctionsService by inject()
    private val logger: TestMakerLogger by inject()
    private val testViewModel: TestViewModel by sharedViewModel()

    private val importFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it ?: return@registerForActivityResult
        val (title, content) = TestMakerFileReader.readFileFromUri(it, requireActivity())
        loadTestByText(title = title, text = content)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(REQUEST_WORKBOOK_CREATED) { requestKey, bundle ->
            binding.viewPager.setCurrentItem(0, true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        if (sharedPreferenceManager.isRemovedAd) {
            binding.adView.visibility = View.GONE
        } else {
            binding.adView.loadAd(AdRequest.Builder().build())
        }

        binding.viewPager.offscreenPageLimit = 1
        binding.viewPager.isUserInputEnabled = false
        binding.viewPager.adapter = ViewPagerAdapter(
            requireActivity(),
            listOf(
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

        val navigationView = binding.navView
        navigationView.setNavigationItemSelectedListener { menuItem ->

            when (menuItem.itemId) {
                R.id.nav_import -> importFile.launch(arrayOf("text/*"))
                R.id.nav_remove_ad -> {
                    viewModel.purchaseRemoveAd(
                        requireActivity(),
                        BillingItem(getString(R.string.sku_remove_ad), BillingClient.SkuType.INAPP)
                    )
                }
            }
            false
        }

        val drawerToggle = ActionBarDrawerToggle(
            requireActivity(),
            binding.drawerLayout,
            binding.toolbar,
            R.string.add,
            R.string.add
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        viewModel.startBillingConnection()
        viewModel.billingStatus.observeNonNull(this) {
            when (it) {
                is BillingStatus.Error -> {
                    when (it.responseCode) {
                        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.alrady_removed_ad),
                                Toast.LENGTH_SHORT
                            ).show()
                            binding.adView.visibility = View.GONE
                            viewModel.removeAd()
                        }
                        BillingClient.BillingResponseCode.USER_CANCELED -> Toast.makeText(
                            requireContext(),
                            getString(R.string.purchase_canceled),
                            Toast.LENGTH_SHORT
                        ).show()
                        else -> Toast.makeText(
                            requireContext(),
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

        return binding.root
    }

    private fun loadTestByText(title: String = "", text: String) {
        requireActivity().executeJobWithDialog(
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
                    requireContext(),
                    requireContext().getString(R.string.message_success_load, it.title),
                    Toast.LENGTH_LONG
                ).show()
            },
            onFailure = {
                requireContext().showErrorToast(it)
            }
        )
    }

    private inner class ViewPagerAdapter(
        activity: FragmentActivity,
        private val fragments: List<Fragment>
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = fragments.size
        override fun createFragment(position: Int): Fragment = fragments[position]
    }
}