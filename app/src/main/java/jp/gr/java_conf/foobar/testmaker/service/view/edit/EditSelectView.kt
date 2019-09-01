package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import com.google.android.material.textfield.TextInputLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import io.realm.RealmList
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Select
import kotlinx.android.synthetic.main.layout_edit_select.view.*

class EditSelectView : RelativeLayout{

    private var layoutOthers = arrayOfNulls<TextInputLayout>(Constants.OTHER_SELECT_MAX)

    private var editOthers = arrayOfNulls<EditText>(Constants.OTHER_SELECT_MAX)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        for (i in editOthers.indices) {
            val s = "set_other${i+1}"
            val strId = resources.getIdentifier(s, "id", context.packageName)
            editOthers[i] = findViewById(strId)

            layoutOthers[i] = findViewById(resources.getIdentifier("textInputLayout_other_${i+1}", "id", context.packageName))

        }

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_edit_select, this) }

    fun isFilled(): Boolean{

        if(set_answer_choose.text.toString() == "") return false

        for (i in 0 until layoutOthers.filter {layout -> layout?.visibility == View.VISIBLE }.size) if(editOthers[i]?.text.toString() == "") return false

        return true
    }

    fun getAnswer():String{
        return set_answer_choose.text.toString()
    }

    fun getOthers(): Array<String> {

        return Array(layoutOthers.filter { layout -> layout?.visibility == View.VISIBLE }.size){ i -> editOthers[i]?.text.toString()}

    }

    fun setAnswer(answer: String){
        set_answer_choose.setText(answer)
    }

    fun setOthers(others: RealmList<Select>){

        for (i in 0 until others.size) editOthers[i]?.setText(others[i]?.selection)

    }

    fun reloadOthers(num: Int){

        for (i in layoutOthers.indices) layoutOthers[i]?.visibility = if (i < num) View.VISIBLE else View.GONE
    }

    fun reset(){

        editOthers.forEach { it?.setText("") }

        set_answer_choose.setText("")

    }

    private fun enableAuto(limit: Int){

        for (i in 0 until limit) {
            editOthers[i]?.setText(context.getString(R.string.state_auto))
            editOthers[i]?.isEnabled = false

        }
    }

    private fun disableAuto(limit: Int){

        for (i in 0 until limit) {
            if (editOthers[i]?.text.toString() == context.getString(R.string.state_auto)) editOthers[i]?.setText("")

            editOthers[i]?.isEnabled = true

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