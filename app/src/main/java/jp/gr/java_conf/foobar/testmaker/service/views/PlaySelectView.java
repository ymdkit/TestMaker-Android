package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;

public class PlaySelectView extends LinearLayout {

    Button[] buttonChoices;
    TextView[] textChoices;

    LinearLayout layoutPlaySelect;

    public interface OnClickListener {
        void onClick(String answer);
    }

    @Nullable
    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public PlaySelectView(Context context) {
        super(context);
    }

    public PlaySelectView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_select, this);

        layoutPlaySelect = layout.findViewById(R.id.layout_play_select);

        buttonChoices = new Button[6];
        textChoices = new TextView[6];
        for (int i = 0; i < buttonChoices.length; i++) {
            String s = "button" + String.valueOf(i + 1);
            int strId = getResources().getIdentifier(s, "id", context.getPackageName());
            buttonChoices[i] = layout.findViewById(strId).findViewById(R.id.button);
            textChoices[i] = layout.findViewById(strId).findViewById(R.id.text);
            buttonChoices[i].setTag(i);
            textChoices[i].setTag(i);

            if (Build.VERSION.SDK_INT >= 21) {
                buttonChoices[i].setStateListAnimator(null);
            }

            buttonChoices[i].setOnClickListener(view -> {

                if (listener != null) {

                    listener.onClick((String) textChoices[(int) view.getTag()].getText());

                }

            });

        }
    }

    public PlaySelectView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void invalidate (){

        for (final Button button : buttonChoices) {

            button.setClickable(false);

            new Handler().postDelayed(() -> button.setClickable(true), 600);

        }
    }

    public void setTextChoices(Quest question,ArrayList<String> autoChoices){

        ArrayList<String> choices = new ArrayList<>();

        if (question.getAuto()) {

            choices = autoChoices;

        } else {

            for (int i = 0; i < question.getSelections().size(); i++) {
                choices.add(question.getSelections().get(i).getSelection());
            }
        }
        choices.add(question.getAnswer());
        Collections.shuffle(choices);

        for (int i = 0; i < question.getSelections().size() + 1; i++) {
            buttonChoices[i].setTag(i);
            textChoices[i].setText(choices.get(i));

        }

    }

    public void show(Quest question){

        layoutPlaySelect.setVisibility(VISIBLE);

        for (int i = 0; i < buttonChoices.length; i++) {
            if (i < question.getSelections().size() + 1) {
                buttonChoices[i].setVisibility(View.VISIBLE);
                textChoices[i].setVisibility(View.VISIBLE);
            } else {
                buttonChoices[i].setVisibility(View.GONE);
                textChoices[i].setVisibility(View.GONE);
            }
        }
    }

    public void hide(){

        layoutPlaySelect.setVisibility(GONE);

    }



}
