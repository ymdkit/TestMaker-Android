package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.ui.core.showToast
import com.google.android.gms.ads.AdRequest
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentHomeBinding
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
                // todo 問題作成画面に移動させる
                R.id.nav_import -> importFile.launch(arrayOf("text/*"))
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