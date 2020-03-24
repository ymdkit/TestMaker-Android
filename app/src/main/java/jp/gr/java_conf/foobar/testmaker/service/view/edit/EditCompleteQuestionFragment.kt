package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditCompleteQuestionBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditCompleteQuestionFragment : EditQuestionFragment() {
    override val editQuestionViewModel: EditCompleteQuestionViewModel by sharedViewModel()

    private var binding: FragmentEditCompleteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return DataBindingUtil.inflate<FragmentEditCompleteQuestionBinding>(inflater, R.layout.fragment_edit_complete_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                saveQuestion()
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
