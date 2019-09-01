package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.layout_play_review.view.*

class PlayReviewView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_play_review, this) }

    fun setTextAnswer(answer: String) { text_answer.text = context.getString(R.string.message_answer, answer) }

    fun setTextExplanation(explanation: String) {

        text_explanation.visibility = View.VISIBLE

        text_explanation.text = context.getString(R.string.explanation, explanation)

        if (explanation == "") text_explanation.visibility = View.GONE

    }

}
