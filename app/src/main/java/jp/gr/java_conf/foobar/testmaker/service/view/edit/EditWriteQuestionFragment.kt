package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.FragmentEditWriteQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class EditWriteQuestionFragment : EditQuestionFragment() {
    private val editWriteQuestionViewModel: EditWriteQuestionViewModel by sharedViewModel()
    private val testViewModel: TestViewModel by sharedViewModel()

    private var binding: FragmentEditWriteQuestionBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        editWriteQuestionViewModel.imagePath.observeNonNull(viewLifecycleOwner) {
            if (it.isEmpty()) binding?.buttonImage?.setImageResource(R.drawable.ic_insert_photo_white_24dp)
        }

        return DataBindingUtil.inflate<FragmentEditWriteQuestionBinding>(inflater, R.layout.fragment_edit_write_question, container, false).apply {
            binding = this
            lifecycleOwner = viewLifecycleOwner
            viewModel = editWriteQuestionViewModel
            commonViewModel = editQuestionViewModel

            buttonAdd.setOnClickListener {
                requireContext().showToast(getString(R.string.msg_save))
                if (editWriteQuestionViewModel.selectedQuestion.id == Question().id) {
                    testViewModel.create(testViewModel.get(editWriteQuestionViewModel.testId), editWriteQuestionViewModel.createQuestion())
                } else {
                    testViewModel.update(editWriteQuestionViewModel.createQuestion())
                    requireActivity().finish()
                }

                editWriteQuestionViewModel.formReset()
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
