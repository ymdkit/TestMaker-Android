package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.firestore.DocumentSnapshot
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.AccountMainFragmentBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseMyPageViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountMainFragment(private val listener: OnTestDownloadedListener) : Fragment() {

    private val viewModel: FirebaseMyPageViewModel by viewModel()
    internal lateinit var controller: AccountMainController
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: AccountMainFragmentBinding? = null

    interface OnTestDownloadedListener {
        fun onDownloaded()
    }

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
                lifecycleScope.launch {

                    val dialog = AlertDialog.Builder(requireActivity())
                            .setTitle(getString(R.string.downloading))
                            .setView(LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_progress, requireActivity().findViewById(R.id.layout_progress))).show()

                    when (val result = viewModel.downloadTest(document.id)) {
                        is FirebaseTestResult.Success -> {
                            viewModel.convert(result.test)

                            Toast.makeText(requireActivity(), getString(R.string.msg_success_download_test, result.test.name), Toast.LENGTH_SHORT).show()
                            listener.onDownloaded()
                        }
                        is FirebaseTestResult.Failure -> {
                            Toast.makeText(requireActivity(), result.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    dialog.dismiss()

                }
            }

            override fun onClickDetailTest(document: DocumentSnapshot) {
                val data = document.toObject(FirebaseTest::class.java) ?: return

                val dialogLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_online_test_info, requireActivity().findViewById(R.id.layout_dialog_info))

                val textInfo = dialogLayout.findViewById<TextView>(R.id.text_info)
                textInfo.text = getString(R.string.info_firebase_test, data.userName, data.getDate(), data.overview)

                AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                        .setView(dialogLayout)
                        .setTitle(data.name)
                        .show()
            }

            override fun onClickDeleteTest(document: DocumentSnapshot) {
                AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                        .setTitle(getString(R.string.delete_exam))
                        .setMessage(getString(R.string.message_delete_exam, document.toObject(FirebaseTest::class.java)?.name))
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            viewModel.deleteTest(document.id)
                            viewModel.fetchMyTests()
                        }
                        .setNegativeButton(android.R.string.cancel, null)
                        .create().show()
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
                viewModel.getUser()?.let {
                    if (testViewModel.tests.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.message_non_exist_test), Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }

                    showDialogUpload()

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
                    layoutNotLogin.visibility = View.VISIBLE
                    progress.isRefreshing = false
                    return@setOnRefreshListener
                }
                viewModel.fetchMyTests()
            }

            recyclerView.adapter = controller.adapter

        }.root
    }

    private fun showDialogUpload() {

        var position = 0

        val dialogLayout = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_upload, requireActivity().findViewById(R.id.layout_dialog_upload))

        val spinner = dialogLayout.findViewById<Spinner>(R.id.spinner)
        val editOverView = dialogLayout.findViewById<EditText>(R.id.edit_overview)
        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, testViewModel.tests.map { it.title }.toTypedArray())

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(parent: AdapterView<*>?,
                                        view: View?, positionSpinner: Int, id: Long) {
                position = positionSpinner
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dialog = AlertDialog.Builder(requireActivity(), R.style.MyAlertDialogStyle)
                .setView(dialogLayout)
                .setTitle(getString(R.string.message_upload_test))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .show()

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener { it ->
                    it.isEnabled = false

                    lifecycleScope.launch {

                        val progress = AlertDialog.Builder(requireActivity())
                                .setTitle(getString(R.string.uploading))
                                .setView(LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_progress, requireActivity().findViewById(R.id.layout_progress))).show()

                        viewModel.uploadTest(RealmTest.createFromTest(testViewModel.tests[position]), editOverView.text.toString())

                        Toast.makeText(requireContext(), getString(R.string.msg_test_upload), Toast.LENGTH_SHORT).show()
                        viewModel.getMyTests()
                        dialog.dismiss()
                        progress.dismiss()

                    }
                }
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