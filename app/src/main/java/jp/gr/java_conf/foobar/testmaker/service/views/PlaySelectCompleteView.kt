package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import kotlinx.android.synthetic.main.layout_play_select_complete.view.*

class PlaySelectCompleteView : LinearLayout {

    private var checkBoxes: Array<CheckBox?> = arrayOfNulls(6)

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        button_ok.setOnClickListener {

            if (listener != null) listener!!.onClick()

        }

        if (Build.VERSION.SDK_INT >= 21) button_ok.stateListAnimator = null

        for (i in checkBoxes.indices) {
            val s = "check_select_" + (i + 1).toString()
            val strId = resources.getIdentifier(s, "id", context.packageName)
            checkBoxes[i] = findViewById(strId)
            checkBoxes[i]!!.tag = i

        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init{

        LayoutInflater.from(context).inflate(R.layout.layout_play_select_complete, this)

    }

    override fun invalidate() {

        for (button in checkBoxes) {

            button!!.isClickable = false

            Handler().postDelayed({ button.isClickable = true }, 600)

        }
    }

    fun setTextChoices(question: Quest, autoChoices: ArrayList<String>) {

        var choices = ArrayList<String>()

        if (question.auto) {

            choices = autoChoices

        } else {

            question.selections.forEach{choices.add(it.selection)}

        }

        question.answers.forEach { choices.add(it.selection) }

        choices.shuffle()

        for (i in 0 until question.selections.size + question.answers.size) {

            checkBoxes[i]!!.tag = i
            checkBoxes[i]!!.text = choices[i]

        }

    }

    fun show(question: Quest) {

        visibility = View.VISIBLE

        for (i in checkBoxes.indices) {
            if (i < question.selections.size + question.answers.size) {
                checkBoxes[i]!!.visibility = View.VISIBLE
            } else {
                checkBoxes[i]!!.visibility = View.GONE
            }
        }
    }

    fun getAnswers() : ArrayList<String?>{

        val array = ArrayList<String?>()

        checkBoxes.filter { box -> box?.isChecked!! }.forEach { array.add(it?.text.toString()) }

        return array
    }

}
