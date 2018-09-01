package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.layout_play_manual.view.*

class PlayManualView : LinearLayout {
    
    private var listener: PlayManualView.OnClickListener? = null

    interface OnClickListener {
        fun onClickRight()
        fun onClickMistake()
    }

    fun setOnClickListener(listener: PlayManualView.OnClickListener) {
        this.listener = listener
    }


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        button_right.setOnClickListener { if (listener != null) listener!!.onClickRight() }

        button_mistake.setOnClickListener { if (listener != null) listener!!.onClickMistake() }

        if (Build.VERSION.SDK_INT >= 21) {

            button_right.stateListAnimator = null
            button_mistake.stateListAnimator = null

        }

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_play_manual, this) }

}
