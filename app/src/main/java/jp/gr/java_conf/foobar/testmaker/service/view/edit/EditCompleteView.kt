package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import kotlin.math.min

class EditCompleteView: RelativeLayout{

    private var layoutAnswers = arrayOfNulls<TextInputLayout>(Constants.ANSWER_MAX)

    var answers = arrayOfNulls<EditText>(Constants.ANSWER_MAX)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        for (i in answers.indices) {
            val s = "set_answer_write_${i+1}"
            val strId = resources.getIdentifier(s, "id", context.packageName)
            answers[i] = findViewById(strId)

            layoutAnswers[i] = findViewById(resources.getIdentifier("textInputLayout_answer_complete_${i+1}", "id", context.packageName))

        }


    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_edit_complete, this) }

    fun reloadAnswers(num: Int) {

        for (i in layoutAnswers.indices) layoutAnswers[i]?.visibility = if (i < num) View.VISIBLE else View.GONE

    }

    fun setAnswers(question: Quest){

        for (i in 0 until min(Constants.ANSWER_MAX,question.answers.size)) answers[i]?.setText(question.answers[i]?.selection)

    }

    fun getAnswers(): Array<String> {

        return Array(layoutAnswers.filter { layout -> layout?.visibility == View.VISIBLE }.size){ i -> answers[i]?.text.toString()}
    }

    fun isFilled(): Boolean{

        for (i in 0 until layoutAnswers.filter {layout -> layout?.visibility == View.VISIBLE }.size) if(answers[i]?.text.toString() == "") return false

        return true

    }

    fun isDuplicate () :Boolean{

        return getAnswers().distinct().size != getAnswers().size

    }

    fun reset(){

        answers.forEach { it?.setText("") }

    }
}