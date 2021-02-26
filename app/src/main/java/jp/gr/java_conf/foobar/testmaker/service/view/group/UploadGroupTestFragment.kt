package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentUploadGroupTestBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.LoadingDialogFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadGroupTestFragment : Fragment() {

    private lateinit var binding: FragmentUploadGroupTestBinding
    private val testViewModel: TestViewModel by viewModel()
    private val viewModel: FirebaseViewModel by viewModel()
    private val args: GroupDetailFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, testViewModel.tests.map { it.title }.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        return DataBindingUtil.inflate<FragmentUploadGroupTestBinding>(inflater, R.layout.fragment_upload_group_test, container, false).apply {
            binding = this
            spinner.adapter = adapter

            buttonUpload.setOnClickListener {

                var dialog: LoadingDialogFragment? = null

                val job = lifecycleScope.launch {
                    viewModel.uploadTestInGroup(
                            RealmTest.createFromTest(testViewModel.tests[binding.spinner.selectedItemPosition]),
                            binding.editOverview.text.toString(),
                            args.groupId)
                    Toast.makeText(requireContext(), getString(R.string.msg_test_upload), Toast.LENGTH_SHORT).show()

                    withContext(Dispatchers.Main) {
                        dialog?.dismiss()
                        findNavController().popBackStack()
                    }
                }
                dialog = LoadingDialogFragment(
                        title = getString(R.string.uploading),
                        onCanceled = {
                            Toast.makeText(requireContext(), getString(R.string.msg_canceled), Toast.LENGTH_SHORT).show()
                            job.cancel()
                        }
                )
                dialog.show(requireActivity().supportFragmentManager, "TAG")
            }

        }.root
    }
}