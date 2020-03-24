package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditSelectCompleteQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditSelectCompleteQuestionFragment : EditQuestionFragment() {
    override val editQuestionViewModel: EditSelectCompleteQuestionViewModel by sharedViewModel()

    private var binding: FragmentEditSelectCompleteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        editQuestionViewModel.isCheckedAuto.observeNonNull(viewLifecycleOwner) { isAuto ->
            editQuestionViewModel.others.forEach {
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
            viewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                saveQuestion()
            }

            buttonAddOther.setOnClickListener {
                editQuestionViewModel.mutateSizeOfOthers(1)
            }

            buttonRemoveOther.setOnClickListener {
                editQuestionViewModel.mutateSizeOfOthers(-1)
            }

            buttonAddAnswer.setOnClickListener {
                editQuestionViewModel.mutateSizeOfAnswers(1)
            }

            buttonRemoveAnswer.setOnClickListener {
                editQuestionViewModel.mutateSizeOfAnswers(-1)
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
