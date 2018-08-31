package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class PlayMistakeView extends LinearLayout {

    TextView textYourAnswer;

    Button buttonNext;

    LinearLayout layoutPlayMistake;

    Context context;

    public interface OnClickListener {
        void onClick();
    }

    @Nullable
    private PlayMistakeView.OnClickListener listener;

    public void setOnClickListener(PlayMistakeView.OnClickListener listener) {
        this.listener = listener;
    }

    public PlayMistakeView(Context context) {
        super(context);
    }

    public PlayMistakeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_mistake,this);

        textYourAnswer = layout.findViewById(R.id.text_your_answer);

        buttonNext = layout.findViewById(R.id.button_next);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // API level 21 以上
            buttonNext.setStateListAnimator(null);
        }

        buttonNext.setOnClickListener(view -> {

            if(listener!= null) {

                listener.onClick();

            }
        });

        layoutPlayMistake = layout.findViewById(R.id.layout_play_mistake);

    }

    public PlayMistakeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);


    }

    public void setTextYourAnswer(String answer){

        textYourAnswer.setVisibility(VISIBLE);

        textYourAnswer.setText(context.getString(R.string.your_answer, answer));

        if(answer.equals("")) textYourAnswer.setVisibility(GONE);

    }
}
