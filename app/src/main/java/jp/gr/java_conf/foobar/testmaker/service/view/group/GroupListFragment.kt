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
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class GroupListFragment : Fragment() {

    private val controller: GroupListController by lazy { GroupListController() }

    private val viewModel: GroupListViewModel by viewModel()
    private val auth: Auth by inject()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val user = auth.getUser()
        val uid = user?.uid

        lifecycleScope.launch {
            auth.getUser()?.uid?.let {
                controller.groups = viewModel.getGroups(it)
            }
        }

        return DataBindingUtil.inflate<FragmentGroupListBinding>(inflater, R.layout.fragment_group_list, container, false).apply {
            recyclerView.adapter = controller.adapter
        }.root
    }
}