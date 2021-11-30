package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdRequest
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupContainerBinding
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import org.koin.android.ext.android.inject

class GroupContainerFragment: Fragment() {

    val sharedPreferenceManager: SharedPreferenceManager by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = DataBindingUtil.inflate<FragmentGroupContainerBinding>(
            inflater,
            R.layout.fragment_group_container,
            container,
            false
        )

        if (sharedPreferenceManager.isRemovedAd) {
            binding.adView.visibility = View.GONE
        } else {
            binding.adView.loadAd(AdRequest.Builder().build())
        }

        childFragmentManager.findFragmentById(binding.fragmentNav.id)?.let {
            binding.toolbar.setupWithNavController(it.findNavController())
        }

        return binding.root
    }

}