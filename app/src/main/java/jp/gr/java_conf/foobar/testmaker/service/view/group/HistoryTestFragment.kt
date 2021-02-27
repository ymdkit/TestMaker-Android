package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentHistoryTestBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class HistoryTestFragment : Fragment() {

    private val controller by lazy { TestHistoryController(requireContext()) }

    private val args: HistoryTestFragmentArgs by navArgs()

    private val viewModel: HistoryTestViewModel by viewModel()


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