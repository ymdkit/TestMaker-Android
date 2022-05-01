package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentHistoryTestBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryTestFragment : Fragment() {

    private val controller by lazy { TestHistoryController(requireContext()) }

    private val args: HistoryTestFragmentArgs by navArgs()

    private val viewModel: HistoryTestViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        refresh()

        return DataBindingUtil.inflate<FragmentHistoryTestBinding>(inflater, R.layout.fragment_history_test, container, false).apply {
            recyclerView.adapter = controller.adapter
        }.root
    }

    private fun refresh() = lifecycleScope.launch {
        val histories = viewModel.getHistories(args.documentId)
        controller.histories = histories

    }
}