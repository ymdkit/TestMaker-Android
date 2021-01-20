package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.AccountMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinkCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.UploadTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.MenuItem
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountMainFragment : Fragment() {

    private val viewModel: FirebaseMyPageViewModel by viewModel()
    private val testViewModel: TestViewModel by sharedViewModel()
    private val firebaseAnalytic: FirebaseAnalytics by inject()

    var listener: OnTestDownloadedListener? = null

    private var binding: AccountMainFragmentBinding? = null

    private val controller: AccountMainController by lazy {
        AccountMainController(requireContext())
    }

    interface OnTestDownloadedListener {
        fun onDownloaded()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? OnTestDownloadedListener
        if (listener == null) {
            throw ClassCastException("$context must implement OnArticleSelectedListener")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        viewModel.getMyTests().observeNonNull(this) {
            controller.tests = it
            binding?.progress?.isRefreshing = false
        }

        controller.setOnClickListener(object : AccountMainController.OnClickListener {

            override fun onClickTest(document: DocumentSnapshot) {
                ListDialogFragment(
                        document.toObject(FirebaseTest::class.java)?.name ?: "",
                        listOf(
                                MenuItem(title = getString(R.string.download), iconRes = R.drawable.ic_file_download_white, action = { downloadTest(document) }),
                                MenuItem(title = getString(R.string.share), iconRes = R.drawable.ic_share_white, action = { shareTest(document) }),
                                MenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteTest(document) })
                        )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }
        })

        return DataBindingUtil.inflate<AccountMainFragmentBinding>(inflater, R.layout.account_main_fragment, container, false).apply {
            binding = this
            recyclerView.adapter = controller.adapter
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel

            viewModel.getUser()?.let {
                viewModel.isLogin.value = true
            }

            buttonLogin.setOnClickListener {
                firebaseAnalytic.logEvent("login_from_account_tab", Bundle())
                startActivityForResult(
                        viewModel.getAuthUIIntent(),
                        REQUEST_SIGN_IN)
            }

            fab.setOnClickListener {
                viewModel.getUser()?.let {
                    if (testViewModel.tests.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.message_non_exist_test), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    UploadTestActivity.startActivity(requireActivity())
                } ?: run {
                    AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                            .setTitle(getString(R.string.login))
                            .setMessage(getString(R.string.msg_not_login))
                            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                                startActivityForResult(
                                        viewModel.getAuthUIIntent(),
                                        MainActivity.REQUEST_SIGN_IN)
                            }
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show()
                }
            }

            progress.setOnRefreshListener {
                viewModel.getUser() ?: run {
                    controller.tests = emptyList()
                    viewModel.isLogin.value = false
                    progress.isRefreshing = false
                    return@setOnRefreshListener
                }
                viewModel.fetchMyTests()
            }

        }.root
    }

    fun downloadTest(document: DocumentSnapshot) {
        lifecycleScope.launch {

            val dialog = AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.downloading))
                    .setView(LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_progress, requireActivity().findViewById(R.id.layout_progress))).show()

            when (val result = viewModel.downloadTest(document.id)) {
                is FirebaseTestResult.Success -> {
                    viewModel.convert(result.test)

                    Toast.makeText(requireActivity(), getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                    listener?.onDownloaded()
                }
                is FirebaseTestResult.Failure -> {
                    Toast.makeText(requireActivity(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()

        }
    }

    fun shareTest(document: DocumentSnapshot) {
        firebaseAnalytic.logEvent("upload_from_share_remote", Bundle())
        val data = document.toObject(FirebaseTest::class.java) ?: return
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_test, data.name, DynamicLinkCreator.createDynamicLink(document.id)))
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)

    }

    fun deleteTest(document: DocumentSnapshot) {
        ConfirmDangerDialogFragment(getString(R.string.message_delete_exam, document.toObject(FirebaseTest::class.java)?.name)) {
            viewModel.deleteTest(document.id)
            viewModel.fetchMyTests()
        }.show(requireActivity().supportFragmentManager, "TAG")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == MainActivity.REQUEST_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                viewModel.isLogin.value = true
                viewModel.createUser(viewModel.getUser())
                binding?.progress?.isRefreshing = true
                viewModel.fetchMyTests()

                Toast.makeText(requireContext(), getString(R.string.login_successed), Toast.LENGTH_SHORT).show()
            } else {
                response?.error?.errorCode
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyTests()
    }

    companion object {
        const val REQUEST_SIGN_IN = 12346
    }
}