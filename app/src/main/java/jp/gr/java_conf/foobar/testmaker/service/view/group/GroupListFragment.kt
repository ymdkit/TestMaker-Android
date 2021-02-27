package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.IdpResponse
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupListBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.main.AccountMainFragment
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
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

        controller.setOnClickListener(object : GroupListController.OnClickListener {
            override fun onClickGroup(group: Group) {
                findNavController().navigate(GroupListFragmentDirections.actionGroupListToGroupDetail(groupId = group.id))
            }
        })

        return DataBindingUtil.inflate<FragmentGroupListBinding>(inflater, R.layout.fragment_group_list, container, false).apply {
            binding = this
            isLogin = (auth.getUser() != null)

            buttonLogin.setOnClickListener {
                startActivityForResult(
                        auth.getAuthUIIntent(),
                        AccountMainFragment.REQUEST_SIGN_IN)
            }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == MainActivity.REQUEST_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {

                auth.getUser()?.let {
                    binding.isLogin = true
                    viewModel.createUser(it)
                    refresh()
                    Toast.makeText(requireContext(), getString(R.string.login_successed), Toast.LENGTH_SHORT).show()
                }

            } else {
                response?.error?.errorCode
            }
        }
    }
}