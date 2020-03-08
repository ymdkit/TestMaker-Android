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
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.android.synthetic.main.activity_move_questions.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoveQuestionsActivity : BaseActivity() {

    private val viewModel: MoveQuestionViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    lateinit var questionAdapter: CheckBoxQuestionAdapter

    lateinit var fromTest: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_questions)

        val binding = DataBindingUtil.setContentView<ActivityMoveQuestionsBinding>(this, R.layout.activity_move_questions)
        createAd(binding.adView)

        initToolBar()

        initViews()

        button_save.setOnClickListener {

            if (spinner_to_test.selectedItemPosition - 1 == spinner_from_test.selectedItemPosition) {

                Toast.makeText(baseContext, getString(R.string.msg_same_test), Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            if (testViewModel.tests.isEmpty()) return@setOnClickListener

            val selectedQuestions = questionAdapter.getItems().filterIndexed { index, _ -> questionAdapter.checkBoxStates[index] }

            if (spinner_to_test.selectedItemPosition == 0) {

                val test = Test()
                test.title = getString(R.string.test)
                test.color = ContextCompat.getColor(baseContext, R.color.red)
                val testId = testViewModel.create(test)

                viewModel.addQuestions(testId, selectedQuestions.toTypedArray())

            } else {

                viewModel.addQuestions(testViewModel.tests[spinner_to_test.selectedItemPosition - 1].id, selectedQuestions.toTypedArray())
            }

            if (spinner_actions.selectedItemPosition == 0) {//「移動先」の時

                viewModel.deleteQuestions(fromTest.id, questionAdapter.checkBoxStates)

            }

            finish()

            Toast.makeText(baseContext, getString(R.string.msg_save), Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        val tests = testViewModel.tests

        val fromArray = Array(tests.size) { i -> tests[i].title }

        val fromAdapter = ArrayAdapter(baseContext,
                android.R.layout.simple_spinner_item, fromArray)

        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner_from_test.adapter = fromAdapter

        val toArray = Array(tests.size + 1) { i ->
            if (i == 0) getString(R.string.new_test) else tests[i - 1].title
        }

        val toAdapter = ArrayAdapter(baseContext,
                android.R.layout.simple_spinner_item, toArray)

        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner_to_test.adapter = toAdapter

        spinner_from_test.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                fromTest = tests[position]

                questionAdapter = CheckBoxQuestionAdapter(baseContext, tests[position].questionsNonNull().toTypedArray())

                list_questions.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(applicationContext)
                list_questions.setHasFixedSize(true)
                list_questions.adapter = questionAdapter

                check_all.setOnCheckedChangeListener { _, bool ->

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
