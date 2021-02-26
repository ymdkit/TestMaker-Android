package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentGroupDetailBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.DynamicLinkCreator
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.main.AccountMainFragment
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.ConfirmDangerDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.LoadingDialogFragment
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

            }
            R.id.menu_rename_group -> {


            }
            R.id.menu_delete_group -> {

                group?.let {

                    ConfirmDangerDialogFragment(getString(R.string.msg_delete_group, it.name)) {
                        deleteAndExitGroup(it)

                    }.show(requireActivity().supportFragmentManager, "TAG")

                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun refresh() = lifecycleScope.launch {
        controller.tests = viewModel.getTests(args.groupId)
        group = viewModel.getGroup(args.groupId)
        binding.swipeRefresh.isRefreshing = false
        requireActivity().invalidateOptionsMenu()
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

    fun downloadTest(document: DocumentSnapshot) {

        var dialog: LoadingDialogFragment? = null
        val job = lifecycleScope.launch {
            when (val result = viewModel.downloadTest(document.id)) {
                is FirebaseTestResult.Success -> {
                    viewModel.convert(result.test)

                    Toast.makeText(requireActivity(), getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()

                    Toast.makeText(requireContext(), getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                    val intent = Intent(requireActivity(), MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(intent)
                }
                is FirebaseTestResult.Failure -> {
                    Toast.makeText(requireActivity(), result.message, Toast.LENGTH_SHORT).show()
                }
            }
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
            }
        }
        dialog = LoadingDialogFragment(
                title = getString(R.string.downloading),
                onCanceled = {
                    requireContext().showToast(getString(R.string.msg_canceled))
                    job.cancel()
                }
        )
        dialog.show(requireActivity().supportFragmentManager, "TAG")
    }

    fun shareTest(document: DocumentSnapshot) {
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
            refresh()
            requireContext().showToast(getString(R.string.msg_success_delete_test))
        }.show(requireActivity().supportFragmentManager, "TAG")
    }
}