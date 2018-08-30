package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;

public class PlayCompleteView extends LinearLayout {

    EditText[] editAnswers;

    RelativeLayout layoutPlayComplete;

    SharedPreferenceManager sharedPreferenceManager;

    public PlayCompleteView(Context context) {
        super(context);
    }

    public PlayCompleteView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_complete, this);

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

        layoutPlayComplete = layout.findViewById(R.id.answer_write_and);

        sharedPreferenceManager = new SharedPreferenceManager(context);

        editAnswers = new EditText[4];
        for (int i = 0; i < editAnswers.length; i++) {
            String s = "set_answer_" + String.valueOf(i + 1);
            int strId = getResources().getIdentifier(s, "id", context.getPackageName());
            editAnswers[i] = layout.findViewById(strId);
            editAnswers[i].setOnFocusChangeListener((v, hasFocus) -> {
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
        }
    }

    public PlayCompleteView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void hide (){

        layoutPlayComplete.setVisibility(GONE);

    }

    public void show(){

        layoutPlayComplete.setVisibility(VISIBLE);

    }

    public void initEditAnswers(Quest question){

        for (int i = 0; i < editAnswers.length; i++) {
            if (i < question.getSelections().size()) {

                editAnswers[i].setVisibility(View.VISIBLE);
                editAnswers[i].setText("");

                if (sharedPreferenceManager.isManual()) {
                    editAnswers[i].setVisibility(View.GONE);

                }
            } else {

                editAnswers[i].setText("");
                editAnswers[i].setVisibility(View.GONE);

            }
        }

        editAnswers[0].setFocusable(true);
        editAnswers[0].requestFocus();

    }

    public EditText getFirstEditText(){

        return editAnswers[0];

    }

    public String[] getAnswers(int size){

        String strings[] = new String[size];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = String.valueOf(editAnswers[i].getText());

        }

        return strings;

    }

}
