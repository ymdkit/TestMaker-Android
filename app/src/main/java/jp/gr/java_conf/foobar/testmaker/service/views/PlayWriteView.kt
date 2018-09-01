package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.layout_play_write.view.*

class PlayWriteView : LinearLayout {

    private var listener: PlayWriteView.OnClickListener? = null

    interface OnClickListener {
        fun onClick(answer: String)
    }

    fun setOnClickListener(listener: PlayWriteView.OnClickListener) {
        this.listener = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        edit_answer.clearFocus()
        edit_answer.setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus) {
                // ソフトキーボードを表示する
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                // ソフトキーボードを閉じる
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }// フォーカスが外れたとき
        }

        button_judge.setOnClickListener {

            if (listener != null) listener!!.onClick(edit_answer.text.toString())

        }

        if (Build.VERSION.SDK_INT >= 21) button_judge.stateListAnimator = null

    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_play_write, this)
    }

    fun show() {

        visibility = View.VISIBLE

        edit_answer.setText("")
        edit_answer.isFocusable = true
        edit_answer.requestFocus()

    }

    fun hideKeyboard(){

        edit_answer.clearFocus()

    }
}
