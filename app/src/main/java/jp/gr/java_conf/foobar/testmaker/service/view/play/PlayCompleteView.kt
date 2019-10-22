package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import kotlinx.android.synthetic.main.layout_play_complete.view.*

class PlayCompleteView : LinearLayout {

    private var editAnswers: Array<EditText?> = arrayOfNulls(4)
    
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private var listener: OnClickListener? = null

    val firstEditText: EditText get() = editAnswers[0]!!

    interface OnClickListener {
        fun onClick()
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener

    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        sharedPreferenceManager = SharedPreferenceManager(context)

        for (i in editAnswers.indices) {
            val s = "set_answer_" + (i + 1).toString()
            val strId = resources.getIdentifier(s, "id", context.packageName)
            editAnswers[i] = findViewById(strId)
            editAnswers[i]!!.setOnFocusChangeListener { v, hasFocus ->
                
                if (hasFocus) {
                    // ソフトキーボードを表示する
                    inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
                } else {
                    // ソフトキーボードを閉じる
                    inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
                }// フォーカスが外れたとき
            }

        }

        button_judge.setOnClickListener {

            if (listener != null) {

                button_judge.isEnabled = false

                listener!!.onClick()

                Handler().postDelayed({ button_judge.isEnabled = true }, 600)

            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.layout_play_complete, this) }
    
    fun initEditAnswers(question: Quest) {

        for (i in editAnswers.indices) {

            if (i < question.answers.size) {

                editAnswers[i]!!.visibility = View.VISIBLE
                editAnswers[i]!!.setText("")

                if (sharedPreferenceManager.manual) editAnswers[i]!!.visibility = View.GONE

            } else {

                editAnswers[i]!!.setText("")
                editAnswers[i]!!.visibility = View.GONE

            }
        }

        editAnswers[0]!!.isFocusable = true
        editAnswers[0]!!.requestFocus()

    }

    fun getAnswers(): ArrayList<String?> {

        val array = ArrayList<String?>()

        editAnswers.filter { text -> text?.visibility == View.VISIBLE }.forEach { array.add(it?.text.toString()) }

        return array

    }

}
