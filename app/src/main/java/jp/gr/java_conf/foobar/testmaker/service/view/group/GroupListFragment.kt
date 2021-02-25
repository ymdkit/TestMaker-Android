package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupListBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupListFragment : Fragment() {

    private val controller: GroupListController by lazy { GroupListController(requireContext()) }

    private val viewModel: GroupListViewModel by viewModel()
    private val auth: Auth by inject()

    private lateinit var binding: FragmentGroupListBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        refresh()

        return DataBindingUtil.inflate<FragmentGroupListBinding>(inflater, R.layout.fragment_group_list, container, false).apply {
            binding = this
            recyclerView.adapter = controller.adapter

            swipeRefresh.setOnRefreshListener {
                refresh()
            }

            buttonAdd.setOnClickListener {
                EditTextDialogFragment(
                        title = getString(R.string.title_create_group),
                        hint = getString(R.string.hint_group_name))
                { text ->
                    createAndJoinGroup(text)
                }.show(requireActivity().supportFragmentManager, "TAG")
            }

        }.root
    }

    private fun refresh() = lifecycleScope.launch {
        auth.getUser()?.uid?.let {
            controller.groups = viewModel.getGroups(it)
        }
        binding.swipeRefresh.isRefreshing = false
    }

    private fun createAndJoinGroup(groupName: String) = lifecycleScope.launch {
        auth.getUser()?.uid?.let {
            val group = viewModel.createGroup(it, groupName)
            viewModel.joinGroup(it, group)
            refresh()

            withContext(Dispatchers.Main) {
                requireContext().showToast(getString(R.string.msg_success_create_group))
            }
        }
    }
}