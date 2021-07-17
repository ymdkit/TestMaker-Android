package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentUploadGroupTestBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.domain.UploadTestDestination
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.FirebaseViewModel
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UploadGroupTestFragment : Fragment() {

    private lateinit var binding: FragmentUploadGroupTestBinding
    private val testViewModel: TestViewModel by viewModel()
    private val viewModel: FirebaseViewModel by viewModel()
    private val args: GroupDetailFragmentArgs by navArgs()
    private val logger: TestMakerLogger by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, testViewModel.tests.map { it.title }.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        return DataBindingUtil.inflate<FragmentUploadGroupTestBinding>(inflater, R.layout.fragment_upload_group_test, container, false).apply {
            binding = this
            spinner.adapter = adapter

            buttonUpload.setOnClickListener {
                requireActivity().executeJobWithDialog(
                        title = getString(R.string.uploading),
                        task = {
                            viewModel.uploadTestInGroup(
                                    RealmTest.createFromTest(testViewModel.tests[binding.spinner.selectedItemPosition]),
                                    binding.editOverview.text.toString(),
                                    args.groupId)
                        },
                        onSuccess = {
                            logger.logUploadTestEvent(
                                test = testViewModel.tests[binding.spinner.selectedItemPosition],
                                destination = UploadTestDestination.GROUP.title
                            )
                            Toast.makeText(requireContext(), getString(R.string.msg_test_upload), Toast.LENGTH_SHORT).show()
                            findNavController().popBackStack()

                        },
                        onFailure = {
                            Toast.makeText(requireContext(), getString(R.string.msg_canceled), Toast.LENGTH_SHORT).show()
                        }
                )
            }

        }.root
    }
}