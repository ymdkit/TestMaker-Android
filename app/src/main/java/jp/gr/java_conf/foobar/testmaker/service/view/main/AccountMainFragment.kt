package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.stickyheader.StickyHeaderLinearLayoutManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.AccountMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.SignInRequestContract
import jp.gr.java_conf.foobar.testmaker.service.view.online.UploadTestActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountMainFragment : Fragment() {

    private val viewModel: FirebaseMyPageViewModel by viewModel()
    private val testViewModel: TestViewModel by sharedViewModel()
    private val firebaseAnalytic: FirebaseAnalytics by inject()

    private val logger: TestMakerLogger by inject()
    private val dynamicLinksCreator: DynamicLinksCreator by inject()

    var listener: OnTestDownloadedListener? = null

    private var binding: AccountMainFragmentBinding? = null

    private val controller: AccountMainController by lazy {
        AccountMainController(requireContext())
    }

    private val signIn = registerForActivityResult(SignInRequestContract()) {
        it ?: run {
            return@registerForActivityResult
        }
        viewModel.isLogin.value = true
        viewModel.createUser(viewModel.getUser())
        binding?.progress?.isRefreshing = true
        viewModel.fetchMyTests()
        requireContext().showToast(getString(R.string.msg_success_login))
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        viewModel.getMyTests().observeNonNull(this) {
            controller.tests = it
            binding?.progress?.isRefreshing = false
        }

        controller.setOnClickListener(object : AccountMainController.OnClickListener {

            override fun onClickTest(document: DocumentSnapshot) {
                ListDialogFragment.newInstance(
                    document.toObject(FirebaseTest::class.java)?.name ?: "",
                    listOf(
                        DialogMenuItem(
                            title = getString(R.string.download),
                            iconRes = R.drawable.ic_file_download_white,
                            action = { downloadTest(document) }),
                        DialogMenuItem(
                            title = getString(R.string.share),
                            iconRes = R.drawable.ic_share_white,
                            action = { shareTest(document) }),
                        DialogMenuItem(
                            title = getString(R.string.delete),
                            iconRes = R.drawable.ic_delete_white,
                            action = { deleteTest(document) })
                    )
                ).show(requireActivity().supportFragmentManager, "TAG")
            }
        })

        return DataBindingUtil.inflate<AccountMainFragmentBinding>(
            inflater,
            R.layout.account_main_fragment,
            container,
            false
        ).apply {
            binding = this
            recyclerView.layoutManager = StickyHeaderLinearLayoutManager(requireContext())
            recyclerView.adapter = controller.adapter
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel

            viewModel.getUser()?.let {
                viewModel.isLogin.value = true
            }

            buttonLogin.setOnClickListener {
                firebaseAnalytic.logEvent("login_from_account_tab", Bundle())
                signIn.launch(Unit)
            }

            fab.setOnClickListener {
                if (testViewModel.tests.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.message_non_exist_test),
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                UploadTestActivity.startActivity(requireActivity())
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

        requireActivity().executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                viewModel.downloadTest(document.id)
            },
            onSuccess = {
                viewModel.convert(it)

                logger.logCreateTestEvent(it.name, CreateTestSource.SELF_DOWNLOAD.title)
                requireContext().showToast(getString(R.string.msg_success_download_test, it.name))
                listener?.onDownloaded()
            },
            onFailure = {
                requireContext().showToast(getString(R.string.msg_failure_download_test))
            }
        )
    }

    fun shareTest(document: DocumentSnapshot) {
        firebaseAnalytic.logEvent("upload_from_share_remote", Bundle())
        val data = document.toObject(FirebaseTest::class.java) ?: return

        requireActivity().executeJobWithDialog(
            title = getString(R.string.msg_creating_share_test_link),
            task = {
                dynamicLinksCreator.createShareTestDynamicLinks(document.id)
            },
            onSuccess = {
                it.shortLink?.let {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            getString(R.string.msg_share_test, data.name, it)
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            },
            onFailure = {
                requireContext().showToast(getString(R.string.msg_failure_share_test))
            }
        )
    }

    fun deleteTest(document: DocumentSnapshot) {
        ConfirmDangerDialogFragment.newInstance(
            title = getString(
                R.string.message_delete_exam,
                document.toObject(FirebaseTest::class.java)?.name
            ),
            buttonText = getString(R.string.button_delete_confirm),
            completion = {
                lifecycleScope.launch {
                    runCatching {
                        viewModel.deleteTest(document.id)
                    }.onSuccess {
                        viewModel.fetchMyTests()
                        requireContext().showToast(getString(R.string.msg_success_delete_test))
                    }.onFailure {
                        requireContext().showToast(getString(R.string.msg_failure_delete_test))
                    }
                }
            }).show(requireActivity().supportFragmentManager, "TAG")

    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchMyTests()
    }

    companion object {
        const val REQUEST_SIGN_IN = 12346
    }
}