package jp.gr.java_conf.foobar.testmaker.service.view.move

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityMoveQuestionsBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoveQuestionsActivity : BaseActivity() {

    private val testViewModel: TestViewModel by viewModel()

    lateinit var questionAdapter: CheckBoxQuestionAdapter

    lateinit var fromTest: Test

    private val binding: ActivityMoveQuestionsBinding by lazy {
        DataBindingUtil.setContentView(
            this,
            R.layout.activity_move_questions
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_questions)

        createAd(binding.adView)

        initToolBar()

        initViews()

        binding.buttonSave.setOnClickListener {

            if (binding.spinnerToTest.selectedItemPosition - 1 == binding.spinnerFromTest.selectedItemPosition) {

                Toast.makeText(baseContext, getString(R.string.msg_same_test), Toast.LENGTH_SHORT)
                    .show()

                return@setOnClickListener
            }

            if (testViewModel.tests.isEmpty()) return@setOnClickListener

            val selectedQuestions = questionAdapter.getItems()
                .filterIndexed { index, _ -> questionAdapter.checkBoxStates[index] }

            if (binding.spinnerToTest.selectedItemPosition == 0) {

                testViewModel.create(
                    Test(
                        title = getString(R.string.test),
                        color = ContextCompat.getColor(baseContext, R.color.red),
                        questions = selectedQuestions,
                        source = CreateTestSource.SELF.title
                    )
                )

            } else {

                val test = testViewModel.tests[binding.spinnerToTest.selectedItemPosition - 1]
                testViewModel.update(
                    test.copy(questions = test.questions + selectedQuestions)
                )
            }

            if (binding.spinnerActions.selectedItemPosition == 0) {//「移動先」の時

                testViewModel.update(
                    fromTest.copy(questions = fromTest.questions.filterIndexed { index, _ -> !questionAdapter.checkBoxStates[index] })
                )

            }

            finish()

            Toast.makeText(baseContext, getString(R.string.msg_save), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        val tests = testViewModel.tests

        val fromArray = Array(tests.size) { i -> tests[i].title }

        val fromAdapter = ArrayAdapter(
            baseContext,
            android.R.layout.simple_spinner_item, fromArray
        )

        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerFromTest.adapter = fromAdapter

        val toArray = Array(tests.size + 1) { i ->
            if (i == 0) getString(R.string.new_test) else tests[i - 1].title
        }

        val toAdapter = ArrayAdapter(
            baseContext,
            android.R.layout.simple_spinner_item, toArray
        )

        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinnerToTest.adapter = toAdapter

        binding.spinnerFromTest.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    fromTest = tests[position]

                    questionAdapter = CheckBoxQuestionAdapter(
                        baseContext,
                        tests[position].questions.toTypedArray()
                    )

                    binding.listQuestions.adapter = questionAdapter

                    binding.checkAll.setOnCheckedChangeListener { _, bool ->

                        questionAdapter.checkBoxStates = Array(questionAdapter.itemCount) { bool }
                        questionAdapter.notifyDataSetChanged()

                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val actionId = item.itemId

        when (actionId) {
            android.R.id.home -> {

                finish()

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
