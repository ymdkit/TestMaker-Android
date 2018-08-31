package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class PlayWriteView extends LinearLayout {

    EditText editAnswer;

    Button buttonJudge;

    public interface OnClickListener {
        void onClick(String answer);
    }

    @Nullable
    private PlayWriteView.OnClickListener listener;

    public void setOnClickListener(PlayWriteView.OnClickListener listener) {
        this.listener = listener;
    }


    public PlayWriteView(Context context) {
        super(context);
    }

    public PlayWriteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_write, this);

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        editAnswer = layout.findViewById(R.id.set_answer);

        editAnswer.clearFocus();
        editAnswer.setOnFocusChangeListener((v, hasFocus) -> {

            if(inputMethodManager == null) return;

            if (hasFocus) {
                // ソフトキーボードを表示する
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
            }
            // フォーカスが外れたとき
            else {
                // ソフトキーボードを閉じる
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        buttonJudge = layout.findViewById(R.id.button_judge);

        buttonJudge.setOnClickListener(view -> {

            if(listener != null){

                listener.onClick(editAnswer.getText().toString());

            }

        });

        if(Build.VERSION.SDK_INT >= 21) buttonJudge.setStateListAnimator(null);


    }

    public PlayWriteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EditText getEditAnswer() {
        return editAnswer;
    }

    public void show(){

        setVisibility(VISIBLE);

        editAnswer.setText("");
        editAnswer.setFocusable(true);
        editAnswer.requestFocus();

    }
}
