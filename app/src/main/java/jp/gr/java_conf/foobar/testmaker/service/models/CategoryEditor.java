package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.views.ColorChooser;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.CategoryAdapter;

/**
 * Created by keita on 2017/05/20.
 */

public class CategoryEditor{

    private Context context;
    private ImageButton buttonColor;
    private Button buttonCate;
    private RealmController realmController;
    private ColorChooser colorChooser;

    private AlertDialog dialogCate;
    private ScrambleAdapter categoryAdapter;

    public CategoryEditor(Context c, Button button, RealmController realm, ScrambleAdapter mainAdapter) {

        context = c;
        buttonCate = button;
        realmController = realm;
        this.categoryAdapter = mainAdapter;
    }

    public void setCategory() {

        final View dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_category,
                null);

        final CategoryAdapter adapter = new CategoryAdapter(context, realmController, categoryAdapter);

        adapter.setOnClickListener((int position) -> {

            buttonCate.setText(realmController.getCateList().get(position).getCategory());

            GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);

            drawable.setColor(realmController.getCateList().get(position).getColor());

            buttonCate.setBackgroundDrawable(drawable);
            buttonColor.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_blue));

            buttonCate.setTag(realmController.getCateList().get(position).getCategory());

            dialogCate.dismiss();

        });

        final RecyclerView mRecyclerView = dialogLayout.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        mRecyclerView.setAdapter(adapter);


        colorChooser = LayoutInflater.from(context).inflate(R.layout.dialog_color,
                null).findViewById(R.id.color_chooser);

        buttonColor = dialogLayout.findViewById(R.id.color);
        buttonColor.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_blue));
        buttonColor.setOnClickListener(v -> {

            final View layout_color = LayoutInflater.from(context).inflate(R.layout.dialog_color,
                    null);
            final AlertDialog dialog_color = setDialog(layout_color, context.getString(R.string.edit_color));

            colorChooser = layout_color.findViewById(R.id.color_chooser);
            colorChooser.setColorId(colorChooser.getColors()[0]);
            colorChooser.setDialog(dialog_color, buttonColor);

            dialog_color.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
            dialog_color.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);

        });

        ImageButton add = dialogLayout.findViewById(R.id.add);
        add.setOnClickListener(v -> {
            EditText e = dialogLayout.findViewById(R.id.set_cate);

            if (e.getText().toString().equals("")) {
                Toast.makeText(context, context.getString(R.string.message_wrong), Toast.LENGTH_SHORT).show();
            } else {

                String cate = e.getText().toString();
                buttonCate.setTag(cate);

                buttonCate.setText(cate);

                GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);

                drawable.setColor(colorChooser.getColorId());

                buttonCate.setBackgroundDrawable(drawable);
                buttonColor.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.button_blue));

                realmController.addCate(e.getText().toString(), colorChooser.getColorId());
                adapter.notifyDataSetChanged();

                dialogCate.dismiss();

            }


        });

        dialogCate = setDialog(dialogLayout, context.getString(R.string.edit_category));

        dialogCate.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);

        Button negative = dialogCate.getButton(DialogInterface.BUTTON_NEGATIVE);
        negative.setOnClickListener(v -> {
            // 場合によっては自分で明示的に閉じる必要がある
            dialogCate.dismiss();

        });

    }

    private AlertDialog setDialog(View dialogLayout, String title) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle);
        builder.setView(dialogLayout);
        builder.setTitle(title);
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);

        return builder.show();
    }

}
