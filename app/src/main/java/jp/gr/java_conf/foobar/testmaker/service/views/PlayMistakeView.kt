package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.layout_play_mistake.view.*

class PlayMistakeView : LinearLayout {

    private var listener: PlayMistakeView.OnClickListener? = null

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(listener: PlayMistakeView.OnClickListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) button_next.stateListAnimator = null

        button_next.setOnClickListener {

            if (listener != null) listener!!.onClick()

        }

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_play_mistake, this) }

    private fun setTextYourAnswer(answer: String) {

        text_your_answer.visibility = View.VISIBLE

        text_your_answer.text = context.getString(R.string.your_answer, answer)

        if (answer == "") text_your_answer.visibility = View.GONE

    }

    fun show(yourAnswer: String) {

        visibility = View.VISIBLE

        setTextYourAnswer(yourAnswer)

    }
}
