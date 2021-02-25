package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupDetailBinding
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.main.AccountMainFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class GroupDetailFragment : Fragment() {

    private val args: GroupDetailFragmentArgs by navArgs()
    private val controller: GroupDetailController by lazy { GroupDetailController(requireContext()) }
    private val viewModel: GroupDetailViewModel by viewModel()
    private val auth: Auth by inject()

    private lateinit var binding: FragmentGroupDetailBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        controller.setOnClickListener(object : GroupDetailController.OnClickListener {
            override fun onClickTest(document: DocumentSnapshot) {
                //todo
            }
        })

        return DataBindingUtil.inflate<FragmentGroupDetailBinding>(inflater, R.layout.fragment_group_detail, container, false).apply {
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
                //todo
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refresh()
    }

    private fun refresh() = lifecycleScope.launch {
        controller.tests = viewModel.getTests(args.groupId)
        binding.swipeRefresh.isRefreshing = false
    }
}