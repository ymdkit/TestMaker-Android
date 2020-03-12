package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import com.google.android.material.textfield.TextInputLayout
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R

class EditSelectCompleteView : RelativeLayout {

    private var textInputLayouts = arrayOfNulls<TextInputLayout>(Constants.SELECT_COMPLETE_MAX)

    private var editSelectCompletes = arrayOfNulls<EditText>(Constants.SELECT_COMPLETE_MAX)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        for (i in editSelectCompletes.indices) {
            val s = "set_select_complete_${i+1}"
            val strId = resources.getIdentifier(s, "id", context.packageName)
            editSelectCompletes[i] = findViewById(strId)

            textInputLayouts[i] = findViewById(resources.getIdentifier("textInputLayout_select_complete_${i+1}", "id", context.packageName))

        }

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_edit_select_complete, this) }

    fun isFilled(): Boolean{

        for (i in 0 until textInputLayouts.filter {layout -> layout?.visibility == View.VISIBLE }.size) if(editSelectCompletes[i]?.text.toString() == "") return false

        return true
    }

    fun getAnswers(): List<String> {
        return List(getNumAnswers()) { i -> editSelectCompletes[i]?.text.toString() }
    }

    fun getOthers(): List<String> {

        return List(textInputLayouts.filter { layout -> layout?.visibility == View.VISIBLE }.size - getNumAnswers()) { i -> editSelectCompletes[i + getNumAnswers()]?.text.toString() }

    }

    private fun getNumAnswers(): Int{
        return editSelectCompletes.filter { editText -> editText?.hint?.contains(context.getString(R.string.hint_answer)) == true}.size
    }

    fun setSelections(answers: List<String>, others: List<String>) {

        editSelectCompletes.filter { editText -> editText?.hint?.contains(context.getString(R.string.hint_answer)) == true }
                .forEachIndexed { index, editText -> editText?.setText(answers[index]) }

        editSelectCompletes.filterIndexed { index, editText -> editText?.hint == context.getString(R.string.hint_other) && textInputLayouts[index]?.visibility == View.VISIBLE }
                .forEachIndexed{index, editText ->
                    editText?.setText(others[index])
                    editText?.isEnabled = true}

    }

    fun reset(){

        editSelectCompletes.forEach { it?.setText("") }

    }

    fun reloadSelects(num: Int){

        for (i in textInputLayouts.indices) textInputLayouts[i]?.visibility = if (i < num) View.VISIBLE else View.GONE

    }

    private fun enableAuto(limit: Int){

        for (i in 0 until limit) {

            if(editSelectCompletes[i]?.hint?.contains(context.getString(R.string.hint_answer)) == true)continue

            editSelectCompletes[i]?.setText(context.getString(R.string.state_auto))
            editSelectCompletes[i]?.isEnabled = false

        }
    }

    private fun disableAuto(limit: Int){

        for (i in 0 until limit) {
            if(i >= editSelectCompletes.size) break
            if(editSelectCompletes[i]?.hint?.contains(context.getString(R.string.hint_answer)) == true)continue

            if (editSelectCompletes[i]?.text.toString() == context.getString(R.string.state_auto)) editSelectCompletes[i]?.setText("")

            editSelectCompletes[i]?.isEnabled = true

        }

    }

    fun setAnswerNum(numAnswers:Int){

        for((index,editText) in editSelectCompletes.withIndex()){

            editText?.isEnabled = true

            if(editText?.isEnabled!! && editText.text.toString() == context.getString(R.string.state_auto))editText.setText("")

            editText.hint = if(index < numAnswers) context.getString(R.string.hint_answer) + (index+1).toString() else context.getString(R.string.hint_other)
        }

    }

    fun setAuto(flg: Boolean,num: Int){

        if(flg){
            enableAuto(num)
        }else{
            disableAuto(num)
        }

    }
}