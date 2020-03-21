package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityEditQuestionBinding
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditQuestionActivity : BaseActivity() {

    private val editQuestionViewModel: EditQuestionViewModel by viewModel()

    private val binding by lazy {
        DataBindingUtil.setContentView<ActivityEditQuestionBinding>(this, R.layout.activity_edit_question).apply {
            lifecycleOwner = this@EditQuestionActivity
            viewModel = editQuestionViewModel
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}
