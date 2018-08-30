package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.gr.java_conf.foobar.testmaker.service.R;

public class PlayProblemView extends LinearLayout {

    TextView textProblem;

    ImageButton imageProblem;

    TextView textNumber;

    FrameLayout layoutImage;

    public PlayProblemView(Context context) {
        super(context);
    }

    public PlayProblemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlayProblemView(Context context, AttributeSet attrs) {
        super(context, attrs);

        View layout = LayoutInflater.from(context).inflate(R.layout.layout_play_problem, this);

        textProblem = layout.findViewById(R.id.problem);

        imageProblem = layout.findViewById(R.id.image_problem);

        textNumber = layout.findViewById(R.id.number);

        layoutImage = layout.findViewById(R.id.layout_image);
    }

    public void setTextProblem(String text){

        textProblem.setText(text);

    }

    public void setTextNumber(String text){

        textNumber.setText(text);
    }

    public void hideImage(){

        layoutImage.setVisibility(View.GONE);

    }

    public void showImage(){

        layoutImage.setVisibility(View.VISIBLE);

    }

    public void initImage(){

        Drawable drawable = imageProblem.getDrawable();
        if (drawable != null) {
            ((BitmapDrawable) drawable).getBitmap().recycle();
        }

        imageProblem.setImageBitmap(null);
        imageProblem.setImageDrawable(null);

    }

    public ImageButton getImageProblem() {
        return imageProblem;
    }
}
