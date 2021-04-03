package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupDetailBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinksCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.view.main.AccountMainFragment
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.EditTextDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    private var group: Group? = null

    private lateinit var binding: FragmentGroupDetailBinding

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        controller.setOnClickListener(object : GroupDetailController.OnClickListener {
            override fun onClickTest(document: DocumentSnapshot) {

                val user = auth.getUser()
                val actions = if (user != null && user.uid == document.toObject(FirebaseTest::class.java)?.userId)
                    listOf(
                            DialogMenuItem(title = getString(R.string.download), iconRes = R.drawable.ic_file_download_white, action = { downloadTest(document) }),
                            DialogMenuItem(title = getString(R.string.history), iconRes = R.drawable.ic_baseline_history_24, action = { actionShowHistory(document) }),
                            DialogMenuItem(title = getString(R.string.share), iconRes = R.drawable.ic_share_white, action = { shareTest(document) }),
                            DialogMenuItem(title = getString(R.string.delete), iconRes = R.drawable.ic_delete_white, action = { deleteTest(document) })
                    )
                else
                    listOf(
                            DialogMenuItem(title = getString(R.string.download), iconRes = R.drawable.ic_file_download_white, action = { downloadTest(document) }),
                            DialogMenuItem(title = getString(R.string.share), iconRes = R.drawable.ic_share_white, action = { shareTest(document) })
                    )

                ListDialogFragment(
                        document.toObject(FirebaseTest::class.java)?.name ?: "",
                        actions
                ).show(requireActivity().supportFragmentManager, "TAG")
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
                findNavController().navigate(GroupDetailFragmentDirections.actionGroupDetailToUploadTest(groupId = args.groupId))
            }

        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_group_detail_guest, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        auth.getUser()?.uid?.let { userId ->
            group?.let { group ->
                if (group.userId == userId) {
                    menu.clear()
                    requireActivity().menuInflater.inflate(R.menu.menu_group_detail_owner, menu)
                }
            }
        }

        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_invite_group -> {
                inviteGroup()
            }
            R.id.menu_rename_group -> {

                group?.let {

                    EditTextDialogFragment(
                            title = getString(R.string.title_rename_group),
                            defaultText = it.name,
                            hint = getString(R.string.hint_group_name))
                    { text ->

                        renameGroup(text, it)

                    }.show(requireActivity().supportFragmentManager, "TAG")

                }

            }
            R.id.menu_delete_group -> {

                group?.let {

                    ConfirmDangerDialogFragment(getString(R.string.msg_delete_group, it.name)) {
                        deleteAndExitGroup(it)

                    }.show(requireActivity().supportFragmentManager, "TAG")

                }
            }
            R.id.menu_exit_group -> {

                group?.let {

                    ConfirmDangerDialogFragment(getString(R.string.msg_exit_group, it.name)) {
                        exitGroup(it.id)
                    }.show(requireActivity().supportFragmentManager, "TAG")
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun refresh() = lifecycleScope.launch {
        controller.tests = viewModel.getTests(args.groupId)
        viewModel.getGroup(args.groupId)
                .addOnSuccessListener {
                    it.toObject(Group::class.java)?.let {
                        group = it
                        joinGroup()
                        binding.swipeRefresh.isRefreshing = false
                        requireActivity().invalidateOptionsMenu()
                    } ?: run {
                        requireContext().showToast(getString(R.string.msg_group_deleted))
                        exitGroup(args.groupId)
                    }
                }
    }

    private fun joinGroup() = lifecycleScope.launch {
        group?.let { group ->
            auth.getUser()?.uid?.let { userId ->
                viewModel.joinGroup(userId, group)
            }
        }
    }

    private fun inviteGroup() {

        val group = group ?: return

        requireActivity().executeJobWithDialog(
                title = getString(R.string.msg_creating_invite_group_link),
                task = {
                    DynamicLinksCreator.createInviteGroupDynamicLinks(group.id)
                },
                onSuccess = {
                    it.shortLink?.let {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_invite_group, group.name, it))
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

                    Toast.makeText(requireActivity(), getString(R.string.msg_success_download_test, it.name), Toast.LENGTH_SHORT).show()

                    Toast.makeText(requireContext(), getString(R.string.msg_success_download_test, it.name), Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
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
                    DynamicLinksCreator.createShareTestDynamicLinks(document.id)
                },
                onSuccess = {
                    it.shortLink?.let {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_test, data.name, it))
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
        ConfirmDangerDialogFragment(
                title = getString(R.string.message_delete_exam, document.toObject(FirebaseTest::class.java)?.name),
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

    private fun actionShowHistory(document: DocumentSnapshot) {
        findNavController().navigate(GroupDetailFragmentDirections.actionGroupDetailToHistoryTest(documentId = document.id))
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