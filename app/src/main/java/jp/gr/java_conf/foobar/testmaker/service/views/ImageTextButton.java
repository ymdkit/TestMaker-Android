package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.gr.java_conf.foobar.testmaker.service.R;

/**
 * Created by keita on 2016/07/11.
 */
public class ImageTextButton extends LinearLayout {

    private OnClickListener listener;

    ImageButton button;

    public ImageTextButton(Context context) {
        super(context);
    }

    public ImageTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ImageTextButton);

        View layout = LayoutInflater.from(context).inflate(R.layout.image_text_button, this);

        button = layout.findViewById(R.id.button);
        button.setImageDrawable(a.getDrawable(R.styleable.ImageTextButton_image));

        TextView text = layout.findViewById(R.id.text);

        if(a.getText(R.styleable.ImageTextButton_text)!=null){
            if(!a.getText(R.styleable.ImageTextButton_text).equals("")) {
                text.setVisibility(VISIBLE);
                text.setText(a.getText(R.styleable.ImageTextButton_text));


            }
        }

        a.recycle();
    }

    public ImageTextButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null){
                button.setPressed(true);
                listener.onClick(this);
            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_UP
                && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER
                || event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if (listener != null){
                button.setPressed(true);
                listener.onClick(this);
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }


}
