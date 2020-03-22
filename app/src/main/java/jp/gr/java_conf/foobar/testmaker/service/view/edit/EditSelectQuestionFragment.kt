package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditWriteQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditSelectQuestionFragment : Fragment() {
    private val editQuestionViewModel: EditQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentEditWriteQuestionBinding>(inflater, R.layout.fragment_edit_write_question, container, false).apply {
            //            lifecycleOwner = viewLifecycleOwner
//            viewModel = editQuestionViewModel
        }

        binding.buttonAdd.setOnClickListener {
            testViewModel.create(testViewModel.get(editQuestionViewModel.testId), Question(question = editQuestionViewModel.question.value
                    ?: ""))
        }

        return binding.root
    }
}
