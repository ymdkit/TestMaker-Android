package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.AccountMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.view.edit.EditTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountMainFragment : Fragment() {

    private val viewModel: FirebaseMyPageViewModel by viewModel()
    internal lateinit var controller: AccountMainController

    private var binding: AccountMainFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel.getMyTests().observeNonNull(this) {
            controller.tests = it
            binding?.layoutNotLogin?.visibility = View.GONE
            binding?.progress?.isRefreshing = false
        }

        controller = AccountMainController(requireContext())
        controller.setOnClickListener(object : AccountMainController.OnClickListener {
            override fun onClickDownloadTest(document: DocumentSnapshot) {
            }

            override fun onClickDetailTest(document: DocumentSnapshot) {
            }

            override fun onClickDeleteTest(document: DocumentSnapshot) {
            }

        })

        return DataBindingUtil.inflate<AccountMainFragmentBinding>(inflater, R.layout.account_main_fragment, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner

            viewModel.getUser() ?: run {
                layoutNotLogin.visibility = View.VISIBLE
            }

            buttonLogin.setOnClickListener {
                startActivityForResult(
                        viewModel.getAuthUIIntent(),
                        REQUEST_SIGN_IN)
            }

            fab.setOnClickListener {
                EditTestActivity.startActivity(requireActivity())
            }

            progress.setOnRefreshListener {
                viewModel.getUser() ?: run {
                    controller.tests = emptyList()
                    layoutNotLogin.visibility = View.VISIBLE
                    progress.isRefreshing = false
                    return@setOnRefreshListener
                }
                viewModel.fetchMyTests()
            }

            recyclerView.adapter = controller.adapter

        }.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == MainActivity.REQUEST_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                viewModel.createUser(viewModel.getUser())
                binding?.progress?.isRefreshing = true
                viewModel.fetchMyTests()

                Toast.makeText(requireContext(), getString(R.string.login_successed), Toast.LENGTH_SHORT).show()
            } else {
                response?.error?.errorCode
            }
        }
    }

    companion object {
        const val REQUEST_SIGN_IN = 12346
    }
}