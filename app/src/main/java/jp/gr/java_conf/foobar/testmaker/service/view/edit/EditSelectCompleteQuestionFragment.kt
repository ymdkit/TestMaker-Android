package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditSelectCompleteQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditSelectCompleteQuestionFragment : EditQuestionFragment() {
    private val editSelectCompleteQuestionViewModel: EditSelectCompleteQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: FragmentEditSelectCompleteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        editSelectCompleteQuestionViewModel.isCheckedAuto.observeNonNull(viewLifecycleOwner) { isAuto ->
            editSelectCompleteQuestionViewModel.others.forEach {
                if (isAuto) {
                    it.value = getString(R.string.hint_other)
                } else if (it.value == getString(R.string.hint_other)) {
                    it.value = ""
                }
            }
        }

        return DataBindingUtil.inflate<FragmentEditSelectCompleteQuestionBinding>(inflater, R.layout.fragment_edit_select_complete_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editSelectCompleteQuestionViewModel

            buttonAdd.setOnClickListener {
                requireContext().showToast(getString(R.string.msg_save))
                if (editSelectCompleteQuestionViewModel.selectedQuestion.id == Question().id) {
                    testViewModel.create(testViewModel.get(editSelectCompleteQuestionViewModel.testId), editSelectCompleteQuestionViewModel.createQuestion())
                } else {
                    testViewModel.update(editSelectCompleteQuestionViewModel.createQuestion())
                    requireActivity().finish()
                }

                editSelectCompleteQuestionViewModel.formReset()
            }

            buttonAddOther.setOnClickListener {
                editSelectCompleteQuestionViewModel.mutateSizeOfOthers(1)
            }

            buttonRemoveOther.setOnClickListener {
                editSelectCompleteQuestionViewModel.mutateSizeOfOthers(-1)
            }

            buttonAddAnswer.setOnClickListener {
                editSelectCompleteQuestionViewModel.mutateSizeOfAnswers(1)
            }

            buttonRemoveAnswer.setOnClickListener {
                editSelectCompleteQuestionViewModel.mutateSizeOfAnswers(-1)
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
