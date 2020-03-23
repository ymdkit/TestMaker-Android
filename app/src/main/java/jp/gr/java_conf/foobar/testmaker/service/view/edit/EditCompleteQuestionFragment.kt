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
    private val editCompleteQuestionViewModel: EditCompleteQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: FragmentEditCompleteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return DataBindingUtil.inflate<FragmentEditCompleteQuestionBinding>(inflater, R.layout.fragment_edit_complete_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editCompleteQuestionViewModel

            buttonAdd.setOnClickListener {
                requireContext().showToast(getString(R.string.msg_save))
                if (editCompleteQuestionViewModel.selectedQuestion.id == Question().id) {
                    testViewModel.create(testViewModel.get(editCompleteQuestionViewModel.testId), editCompleteQuestionViewModel.createQuestion())
                } else {
                    testViewModel.update(editCompleteQuestionViewModel.createQuestion())
                    requireActivity().finish()
                }

                editCompleteQuestionViewModel.formReset()
            }

            buttonAddAnswer.setOnClickListener {
                editCompleteQuestionViewModel.mutateSizeOfOthers(1)
            }

            buttonRemoveAnswer.setOnClickListener {
                editCompleteQuestionViewModel.mutateSizeOfOthers(-1)
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
