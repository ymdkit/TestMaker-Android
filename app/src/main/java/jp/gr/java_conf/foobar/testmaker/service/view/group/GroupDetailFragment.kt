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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.ui.core.DialogMenuItem
import com.example.ui.core.ListDialogFragment
import com.example.ui.core.showToast
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.ads.AdRequest
import com.google.firebase.firestore.DocumentSnapshot
import dagger.hilt.android.AndroidEntryPoint
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupDetailBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.view.main.AccountMainFragment
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
@AndroidEntryPoint
class GroupDetailFragment : Fragment() {

    private val args: GroupDetailFragmentArgs by navArgs()
    private val controller: GroupDetailController by lazy { GroupDetailController(requireContext()) }
    private val viewModel: GroupDetailViewModel by viewModels()

    @Inject
    lateinit var auth: Auth

    @Inject
    lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var group: Group? = null

    private lateinit var binding: FragmentGroupDetailBinding

    @Inject
    lateinit var dynamicLinksCreator: DynamicLinksCreator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        controller.setOnClickListener(object : GroupDetailController.OnClickListener {
            override fun onClickTest(document: DocumentSnapshot) {

                val user = auth.getUser()
                val actions =
                    if (user != null && user.uid == document.toObject(FirebaseTest::class.java)?.userId)
                        listOf(
                            DialogMenuItem(
                                title = getString(R.string.download),
                                iconRes = R.drawable.ic_file_download_white,
                                action = { downloadTest(document) }),
                            DialogMenuItem(
                                title = getString(R.string.detail),
                                iconRes = R.drawable.ic_info_white,
                                action = { actionShowInfo(document) }),
                            DialogMenuItem(
                                title = getString(R.string.history),
                                iconRes = R.drawable.ic_baseline_history_24,
                                action = { actionShowHistory(document) }),
                            DialogMenuItem(
                                title = getString(R.string.share),
                                iconRes = R.drawable.ic_share_white,
                                action = { shareTest(document) }),
                            DialogMenuItem(
                                title = getString(R.string.delete),
                                iconRes = R.drawable.ic_delete_white,
                                action = { deleteTest(document) })
                        )
                    else
                        listOf(
                            DialogMenuItem(
                                title = getString(R.string.download),
                                iconRes = R.drawable.ic_file_download_white,
                                action = { downloadTest(document) }),
                            DialogMenuItem(
                                title = getString(R.string.share),
                                iconRes = R.drawable.ic_share_white,
                                action = { shareTest(document) })
                        )

                ListDialogFragment.newInstance(
                    document.toObject(FirebaseTest::class.java)?.name ?: "",
                    actions
                ).show(requireActivity().supportFragmentManager, "TAG")
            }
        })

        return DataBindingUtil.inflate<FragmentGroupDetailBinding>(
            inflater,
            R.layout.fragment_group_detail,
            container,
            false
        ).apply {
            binding = this
            isLogin = (auth.getUser() != null)

            buttonLogin.setOnClickListener {
                startActivityForResult(
                    auth.getAuthUIIntent(),
                    AccountMainFragment.REQUEST_SIGN_IN
                )
            }

            recyclerView.adapter = controller.adapter

            swipeRefresh.setOnRefreshListener {
                refresh()
            }

            buttonAdd.setOnClickListener {
                findNavController().navigate(
                    GroupDetailFragmentDirections.actionGroupDetailToUploadTest(
                        groupId = args.groupId
                    )
                )
            }

            toolbar.setupWithNavController(
                findNavController(),
                AppBarConfiguration(findNavController().graph)
            )

            if (sharedPreferenceManager.isRemovedAd) {
                adView.visibility = View.GONE
            } else {
                adView.loadAd(AdRequest.Builder().build())
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.menu_invite_group -> {
                    inviteGroup()
                    true
                }
                R.id.menu_rename_group -> {

                    group?.let {

                        EditTextDialogFragment.newInstance(
                            title = getString(R.string.title_rename_group),
                            defaultText = it.name,
                            hint = getString(R.string.hint_group_name)
                        )
                        { text ->

                            renameGroup(text, it)

                        }.show(requireActivity().supportFragmentManager, "TAG")

                    }
                    true

                }
                R.id.menu_delete_group -> {

                    group?.let {

                        ConfirmDangerDialogFragment.newInstance(
                            getString(
                                R.string.msg_delete_group,
                                it.name
                            ),
                            getString(R.string.button_delete_confirm)
                        ) {
                            deleteAndExitGroup(it)

                        }.show(requireActivity().supportFragmentManager, "TAG")

                    }
                    true
                }
                R.id.menu_exit_group -> {

                    group?.let {

                        ConfirmDangerDialogFragment.newInstance(
                            getString(
                                R.string.msg_exit_group,
                                it.name
                            ), getString(R.string.button_delete_confirm)
                        ) {
                            exitGroup(it.id)
                        }.show(requireActivity().supportFragmentManager, "TAG")
                    }
                    true
                }
                else -> {
                    true
                }
            }
        }

        refresh()
    }

    private fun refresh() = lifecycleScope.launch {
        controller.tests = viewModel.getTests(args.groupId)
        group = viewModel.getGroup(args.groupId)

        group ?: run {
            requireContext().showToast(getString(R.string.msg_group_deleted))
            exitGroup(args.groupId)
            return@launch
        }

        joinGroup()
        binding.swipeRefresh.isRefreshing = false

        auth.getUser()?.uid?.let { userId ->
            group?.let { group ->
                if (group.userId == userId) {
                    binding.toolbar.menu.clear()
                    binding.toolbar.inflateMenu(R.menu.menu_group_detail_owner)
                }
            }
        }
    }

    private fun joinGroup() = lifecycleScope.launch {
        group?.let { group ->
            auth.getUser()?.uid?.let { userId ->
                viewModel.joinGroup(userId, group, args.groupId)
            }
        }
    }

    private fun inviteGroup() {

        val group = group ?: return

        requireActivity().executeJobWithDialog(
            title = getString(R.string.msg_creating_invite_group_link),
            task = {
                dynamicLinksCreator.createInviteGroupDynamicLinks(group.id)
            },
            onSuccess = {
                it.shortLink?.let {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(
                            Intent.EXTRA_TEXT,
                            getString(R.string.msg_invite_group, group.name, it)
                        )
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
            },
            onFailure = {
                requireContext().showToast(getString(R.string.msg_failure_invite_group))
            }
        )
    }

    private fun renameGroup(name: String, group: Group) = lifecycleScope.launch {
        viewModel.renameGroup(name, group)
        requireContext().showToast(getString(R.string.msg_success_rename_group))
    }

    private fun deleteAndExitGroup(group: Group) = lifecycleScope.launch {

        auth.getUser()?.uid?.let {
            viewModel.exitGroup(it, group.id)
            viewModel.deleteGroup(group.id)
        }

        withContext(Dispatchers.Main) {
            requireContext().showToast(getString(R.string.msg_success_delete_group))
            findNavController().popBackStack()
        }
    }

    private fun exitGroup(groupId: String) = lifecycleScope.launch {

        auth.getUser()?.uid?.let {
            viewModel.exitGroup(it, groupId)
        }

        withContext(Dispatchers.Main) {
            requireContext().showToast(getString(R.string.msg_success_exit_group))
            findNavController().popBackStack()
        }
    }

    fun downloadTest(document: DocumentSnapshot) {

        requireActivity().executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                viewModel.downloadTest(document.id)
            },
            onSuccess = {
                viewModel.convert(it)

                Toast.makeText(
                    requireActivity(),
                    getString(R.string.msg_success_download_test, it.name),
                    Toast.LENGTH_SHORT
                ).show()

                Toast.makeText(
                    requireContext(),
                    getString(R.string.msg_success_download_test, it.name),
                    Toast.LENGTH_SHORT
                ).show()

                val hostActivity = requireActivity() as? MainActivity
                hostActivity?.navigateHomePage()
            },
            onFailure = {
                requireContext().showToast(getString(R.string.msg_failure_download_test))
            }
        )
    }

    fun shareTest(document: DocumentSnapshot) {
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
                        refresh()
                        requireContext().showToast(getString(R.string.msg_success_delete_test))
                    }.onFailure {
                        requireContext().showToast(getString(R.string.msg_failure_delete_test))
                    }
                }
            }).show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun actionShowInfo(document: DocumentSnapshot) {
        val test = document.toObject(FirebaseTest::class.java) ?: return

        ListDialogFragment.newInstance(
            test.name,
            listOf(
                DialogMenuItem(
                    title = getString(R.string.user_name_online, test.userName),
                    iconRes = R.drawable.ic_account,
                    action = { }),
                DialogMenuItem(
                    title = getString(R.string.date_online, test.getDate()),
                    iconRes = R.drawable.ic_baseline_calendar_today_24,
                    action = { }),
                DialogMenuItem(
                    title = getString(R.string.overview_online, test.overview),
                    iconRes = R.drawable.ic_baseline_description_24,
                    action = { }),
            )
        ).show(requireActivity().supportFragmentManager, "TAG")
    }

    private fun actionShowHistory(document: DocumentSnapshot) {
        findNavController().navigate(
            GroupDetailFragmentDirections.actionGroupDetailToHistoryTest(
                documentId = document.id
            )
        )
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
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.login_successed),
                        Toast.LENGTH_SHORT
                    ).show()
                }

            } else {
                response?.error?.errorCode
            }
        }
    }
}