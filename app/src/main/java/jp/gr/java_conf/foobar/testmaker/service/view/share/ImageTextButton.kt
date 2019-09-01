package jp.gr.java_conf.foobar.testmaker.service.view.share

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R
import kotlinx.android.synthetic.main.image_text_button.view.*

/**
 * Created by keita on 2016/07/11.
 */
class ImageTextButton : LinearLayout {

    private var listener: View.OnClickListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        val a = context.obtainStyledAttributes(attrs,
                R.styleable.ImageTextButton)

        button.setImageDrawable(a.getDrawable(R.styleable.ImageTextButton_image))

        if (a.getText(R.styleable.ImageTextButton_text) != null) {

            if (a.getText(R.styleable.ImageTextButton_text) != "") {

                text.visibility = View.VISIBLE
                text.text = a.getText(R.styleable.ImageTextButton_text)

            }
        }

        a.recycle()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init { LayoutInflater.from(context).inflate(R.layout.image_text_button, this) }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            if (listener != null) {
                button.isPressed = true
                listener!!.onClick(this)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && (event.keyCode == KeyEvent.KEYCODE_DPAD_CENTER || event.keyCode == KeyEvent.KEYCODE_ENTER)) {
            if (listener != null) {
                button.isPressed = true
                listener!!.onClick(this)
            }
        }
        return super.dispatchKeyEvent(event)
    }

    override fun setOnClickListener(listener: View.OnClickListener?) {
        this.listener = listener
    }


}
