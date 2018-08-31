package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class PlayManualView extends LinearLayout {

    Button buttonRight;

    Button buttonMistake;

    public interface OnClickListener {
        void onClickRight();
        void onClickMistake();
    }

    @Nullable
    private PlayManualView.OnClickListener listener;

    public void setOnClickListener(PlayManualView.OnClickListener listener) {
        this.listener = listener;
    }


    public PlayManualView(Context context) {
        super(context);
    }

    public PlayManualView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_manual, this);

        buttonRight = layout.findViewById(R.id.button_right);

        buttonRight.setOnClickListener(view -> {

            if(listener != null){

                listener.onClickRight();

            }

        });

        buttonMistake = layout.findViewById(R.id.button_mistake);


        buttonMistake.setOnClickListener(view -> {

            if(listener != null){

                listener.onClickMistake();

            }

        });

        if(Build.VERSION.SDK_INT >= 21){
            buttonRight.setStateListAnimator(null);
            buttonMistake.setStateListAnimator(null);
        }

    }

    public PlayManualView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
