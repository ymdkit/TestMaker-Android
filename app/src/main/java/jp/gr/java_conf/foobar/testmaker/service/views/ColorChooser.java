package jp.gr.java_conf.foobar.testmaker.service.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;

/**
 * Created by keita on 2018/06/08.
 */

public class ColorChooser extends LinearLayout{

    ImageView[] colors = new ImageView[8];

    private int id_color;

    AlertDialog dialog;
    ImageButton button;

    Context context;

    int colorIds[] = {R.color.red,R.color.orange,R.color.yellow,
            R.color.green,R.color.darkgreen,R.color.blue,R.color.navy,R.color.purple};

    public ColorChooser(final Context context, AttributeSet attr){
        super(context, attr);

        this.context = context;

        View layout = LayoutInflater.from(context).inflate(R.layout.color_chooser, this);

        for (int i = 0; i < colors.length; i++) {
            String s = "imageView" + String.valueOf(i + 1);
            int strId = getResources().getIdentifier(s, "id", context.getPackageName());

            colors[i] = layout.findViewById(strId);

            GradientDrawable bgShape = (GradientDrawable)colors[i].getBackground();
            bgShape.setColor(getResources().getColor(colorIds[i]));

            colors[i].setTag(i);
            colors[i].setOnClickListener(v -> {

                for(ImageView color: colors){
                    color.setImageResource(R.drawable.white);
                }

                colors[((int) v.getTag())].setImageResource(R.drawable.ic_done_white);

                id_color = getColors()[(int) v.getTag()];

                if(dialog != null){
                    GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);

                    drawable.setColor(getColors()[(int) v.getTag()]);
                    button.setBackgroundDrawable(drawable);

                    dialog.dismiss();
                }
            });

            colors[0].setImageResource(R.drawable.ic_done_white);
            id_color = getResources().getColor(colorIds[0]);
            dialog = null;

        }

    }

    public void setId_color(int id){

        id_color = id;

        for(int i=0;i<colors.length;i++){
            if(id == context.getResources().getColor(colorIds[i])){
                colors[i].setImageResource(R.drawable.ic_done_white);
            }else{
                colors[i].setImageResource(R.drawable.white);
            }

        }


    }

    public void setDialog(AlertDialog dialog,ImageButton button){
        this.dialog = dialog;
        this.button = button;
    }

    public int getId_color(){

        return id_color;
    }

    public int[] getColors() {
        int[] colorList;
        TypedArray colors = getResources().obtainTypedArray(R.array.color_list);

        if (colors.length() <= 0) {
            return null;
        }

        // リソースID用の配列を準備
        colorList = new int[colors.length()];
        for (int ii = 0; ii < colors.length(); ii++) {
            // TypedArrayから指定indexのTypedValueを取得する
            TypedValue colorValue = new TypedValue();
            if (colors.getValue(ii, colorValue)) {
                // TypedValueからリソースIDを取得する
                colorList[ii] = context.getResources().getColor(colorValue.resourceId);
            }
        }

        colors.recycle();

        return colorList;
    }
}
