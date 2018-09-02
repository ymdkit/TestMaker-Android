package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import kotlinx.android.synthetic.main.layout_play_select.view.*
import java.util.*

class PlaySelectView : LinearLayout {

    private var buttonChoices: Array<Button?> = arrayOfNulls(6)
    private var textChoices: Array<TextView?> = arrayOfNulls(6)

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(answer: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val buttonPass = button_pass.findViewById<Button>(R.id.button)

        buttonPass.setOnClickListener {

            if (listener != null) listener!!.onClick("")

        }

        if (Build.VERSION.SDK_INT >= 21) buttonPass.stateListAnimator = null

        for (i in buttonChoices.indices) {
            val s = "button" + (i + 1).toString()
            val strId = resources.getIdentifier(s, "id", context.packageName)
            buttonChoices[i] = findViewById<View>(strId).findViewById(R.id.button)
            textChoices[i] = findViewById<View>(strId).findViewById(R.id.text)
            buttonChoices[i]!!.tag = i
            textChoices[i]!!.tag = i

            if (Build.VERSION.SDK_INT >= 21) buttonChoices[i]!!.stateListAnimator = null

            buttonChoices[i]!!.setOnClickListener { view ->

                if (listener != null) listener!!.onClick(textChoices[view.tag as Int]!!.text as String)

            }

        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init{

        LayoutInflater.from(context).inflate(R.layout.layout_play_select, this)

    }

    override fun invalidate() {

        for (button in buttonChoices) {

            button!!.isClickable = false

            Handler().postDelayed({ button.isClickable = true }, 600)

        }
    }

    fun setTextChoices(question: Quest, autoChoices: ArrayList<String>) {

        var choices = ArrayList<String>()

        if (question.auto) {

            choices = autoChoices

        } else {

            for (i in 0 until question.selections.size) {
                choices.add(question.selections[i].selection)
            }
        }
        choices.add(question.answer)
        choices.shuffle()

        for (i in 0 until question.selections.size + 1) {

            buttonChoices[i]!!.tag = i
            textChoices[i]!!.text = choices[i]

        }

    }

    fun show(question: Quest) {

        visibility = View.VISIBLE

        for (i in buttonChoices.indices) {
            if (i < question.selections.size + 1) {
                buttonChoices[i]!!.visibility = View.VISIBLE
                textChoices[i]!!.visibility = View.VISIBLE
            } else {
                buttonChoices[i]!!.visibility = View.GONE
                textChoices[i]!!.visibility = View.GONE
            }
        }
    }

}
