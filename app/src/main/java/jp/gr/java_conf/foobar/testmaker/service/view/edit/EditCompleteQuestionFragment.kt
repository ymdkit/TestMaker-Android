package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditCompleteQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditCompleteQuestionFragment : EditQuestionFragment() {
    override val editQuestionViewModel: EditCompleteQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: FragmentEditCompleteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return DataBindingUtil.inflate<FragmentEditCompleteQuestionBinding>(inflater, R.layout.fragment_edit_complete_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                requireContext().showToast(getString(R.string.msg_save))
                if (editQuestionViewModel.selectedQuestion.id == Question().id) {
                    testViewModel.create(testViewModel.get(editQuestionViewModel.testId), editQuestionViewModel.createQuestion())
                } else {
                    testViewModel.update(editQuestionViewModel.createQuestion())
                    requireActivity().finish()
                }

                editQuestionViewModel.resetForm()
            }

            buttonAddAnswer.setOnClickListener {
                editQuestionViewModel.mutateSizeOfOthers(1)
            }

            buttonRemoveAnswer.setOnClickListener {
                editQuestionViewModel.mutateSizeOfOthers(-1)
            }

            buttonImage.setOnClickListener {
                showAlertImage()
            }

        }.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
