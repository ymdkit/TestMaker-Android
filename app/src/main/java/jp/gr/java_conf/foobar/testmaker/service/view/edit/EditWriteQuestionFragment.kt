package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditWriteQuestionBinding
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditWriteQuestionFragment : EditQuestionFragment() {
    override val editQuestionViewModel: EditWriteQuestionViewModel by sharedViewModel()

    private var binding: FragmentEditWriteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return DataBindingUtil.inflate<FragmentEditWriteQuestionBinding>(inflater, R.layout.fragment_edit_write_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                saveQuestion()
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
