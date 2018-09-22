package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest

class EditCompleteView: RelativeLayout{

    var answers = arrayOfNulls<EditText>(Constants.ANSWER_MAX)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        for (i in answers.indices) {
            val s = "set_answer_write_" + (i + 1).toString()
            val strId = resources.getIdentifier(s, "id", context.packageName)
            answers[i] = findViewById(strId)
        }


    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_edit_complete, this) }

    fun reloadAnswers(num: Int) {
        for (i in answers.indices) answers[i]?.visibility = if (i < num) View.VISIBLE else View.GONE

    }

    fun setAnswers(question: Quest){

        for (i in 0 until question.answers.size) answers[i]?.setText(question.answers[i]?.selection)

    }

    fun getAnswers(): Array<String?> {

        return Array(answers.filter { visibility == View.VISIBLE }.size){ i -> answers[i]?.text.toString()}
    }

    fun isFilled(): Boolean{

        return !answers.any {answer -> visibility == View.VISIBLE && answer?.text.toString() == "" }

    }

    fun reset(){

        for(answer in answers) answer?.setText("")

    }
}