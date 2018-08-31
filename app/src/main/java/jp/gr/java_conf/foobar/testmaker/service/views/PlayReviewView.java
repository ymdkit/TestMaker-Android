package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class PlayReviewView extends LinearLayout{

    TextView textAnswer;
    TextView textExplanation;

    Context context;

    public PlayReviewView(Context context) {
        super(context);
    }

    public PlayReviewView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        this.context = context;

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_review,this);

        textAnswer = layout.findViewById(R.id.text_answer);

        textExplanation = layout.findViewById(R.id.text_explanation);
    }

    public PlayReviewView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setTextAnswer(String answer){

        textAnswer.setText(context.getString(R.string.message_answer,answer));

    }

    public void setTextExplanation(String explanation){

        textExplanation.setVisibility(VISIBLE);

        textExplanation.setText(context.getString(R.string.explanation, explanation));

        if(explanation.equals("")) textExplanation.setVisibility(GONE);

    }

}
