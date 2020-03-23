package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditSelectQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditSelectQuestionFragment : EditQuestionFragment() {
    private val editSelectQuestionViewModel: EditSelectQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: FragmentEditSelectQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        editSelectQuestionViewModel.isCheckedAuto.observeNonNull(viewLifecycleOwner) { isAuto ->
            editSelectQuestionViewModel.others.forEach {
                if (isAuto) {
                    it.value = getString(R.string.hint_other)
                } else if (it.value == getString(R.string.hint_other)) {
                    it.value = ""
                }
            }
        }

        return DataBindingUtil.inflate<FragmentEditSelectQuestionBinding>(inflater, R.layout.fragment_edit_select_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editSelectQuestionViewModel
            commonViewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                requireContext().showToast(getString(R.string.msg_save))
                if (editSelectQuestionViewModel.selectedQuestion.id == Question().id) {
                    testViewModel.create(testViewModel.get(editSelectQuestionViewModel.testId), editSelectQuestionViewModel.createQuestion())
                } else {
                    testViewModel.update(editSelectQuestionViewModel.createQuestion())
                    requireActivity().finish()
                }

                editSelectQuestionViewModel.formReset()
            }

            buttonAddOther.setOnClickListener {
                editSelectQuestionViewModel.mutateSizeOfOthers(1)
            }

            buttonRemoveOther.setOnClickListener {
                editSelectQuestionViewModel.mutateSizeOfOthers(-1)
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
