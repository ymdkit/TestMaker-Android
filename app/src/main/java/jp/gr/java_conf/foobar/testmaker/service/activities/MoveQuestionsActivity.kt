package jp.gr.java_conf.foobar.testmaker.service.activities

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.CheckBoxQuestionAdapter
import kotlinx.android.synthetic.main.activity_move_questions.*

class MoveQuestionsActivity : BaseActivity() {

    lateinit var questionAdapter: CheckBoxQuestionAdapter

    lateinit var fromTest: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_move_questions)

        initToolBar()

        createAd(container)

        val tests = realmController.list

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

                questionAdapter = CheckBoxQuestionAdapter(baseContext, tests[position].getQuestions().toTypedArray())

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

        button_save.setOnClickListener { _ ->

            if(spinner_to_test.selectedItemPosition -1 == spinner_from_test.selectedItemPosition){

                Toast.makeText(baseContext,getString(R.string.msg_same_test),Toast.LENGTH_SHORT).show()

                return@setOnClickListener
            }

            if(realmController.list.size < 1) return@setOnClickListener

            val selectedQuestions = questionAdapter.getItems().filterIndexed { index, _ ->  questionAdapter.checkBoxStates[index]}

            Log.d("selected","${questionAdapter.checkBoxStates.filter { it }.size}")

            if(spinner_to_test.selectedItemPosition == 0){

                val testId = realmController.addTest(getString(R.string.test),ContextCompat.getColor(baseContext,R.color.red),"")

                realmController.addQuestions(testId,selectedQuestions.toTypedArray())

            }else{

                realmController.addQuestions(realmController.list[spinner_to_test.selectedItemPosition -1].id,selectedQuestions.toTypedArray())

            }

            if(spinner_actions.selectedItemPosition == 0){//「移動先」の時

                realmController.removeQuestions(fromTest.id,questionAdapter.checkBoxStates)

                //todo resetorder

            }

            finish()

            Toast.makeText(baseContext,getString(R.string.msg_save),Toast.LENGTH_SHORT).show()
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
