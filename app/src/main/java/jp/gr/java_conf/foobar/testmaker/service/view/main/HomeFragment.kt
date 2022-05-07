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
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.billingclient.api.BillingClient
import com.example.ui.core.showToast
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentHomeBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingItem
import jp.gr.java_conf.foobar.testmaker.service.infra.billing.BillingStatus
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.infra.util.TestMakerFileReader
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment() {

    companion object {
        const val REQUEST_WORKBOOK_CREATED = "request_workbook_created"
    }

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    @Inject
    lateinit var logger: TestMakerLogger

    private val importFile = registerForActivityResult(ActivityResultContracts.OpenDocument()) {
        it ?: return@registerForActivityResult
        val (title, content) = TestMakerFileReader.readFileFromUri(it, requireActivity())
        loadTestByText(title = title, exportedWorkbook = content)
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
            binding.drawerLayout.close()
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
                                    requireContext().showToast(
                                        getString(R.string.msg_remove_ad_success),
                                        Toast.LENGTH_LONG
                                    )
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenCreated {

            viewModel.importWorkbookCompletionEvent
                .receiveAsFlow()
                .onEach {
                    requireContext().showToast(getString(R.string.message_success_load, it))
                }
                .launchIn(this)
        }
    }

    private fun loadTestByText(title: String = "no title", exportedWorkbook: String) {
        viewModel.importWorkbook(
            workbookName = title,
            exportedWorkbook = exportedWorkbook
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