package jp.gr.java_conf.foobar.testmaker.service.views.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;
import net.cattaka.android.adaptertoolbox.adapter.listener.ForwardingListener;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Test;
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton;

/**
 * Created by keita on 2017/05/21.
 */

public class TestAdapter extends ScrambleAdapter.AbsViewHolderFactory<TestAdapter.ViewHolder>  {

    private Context context;

    public interface OnClickListener {
        void onClickPlayTest(long id);
        void onClickEditTest(long id);
        void onClickDeleteTest(long id);
    }

    @Nullable
    private OnClickListener listener;
    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }


    public TestAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public TestAdapter.ViewHolder onCreateViewHolder(@NonNull ScrambleAdapter<?> adapter, @NonNull ViewGroup parent, @NonNull ForwardingListener<ScrambleAdapter<?>, RecyclerView.ViewHolder> forwardingListener) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_test, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScrambleAdapter adapter, @NonNull ViewHolder holder,
                                 final int position, Object object) {

        final Test data = (Test)adapter.getItemAt(position);

        holder.title.setText(data.getTitle());

        holder.num.setText(context.getString(R.string.number_existing_questions,data.getQuestionsCorrectCount(),data.getQuestions().size()));

        holder.play.setOnClickListener(view -> {

            if (listener != null) {
                listener.onClickPlayTest(data.getId());
            }

        });

        holder.edit.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClickEditTest(data.getId());
            }
        });

        holder.delete.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClickDeleteTest(data.getId());
            }

        });

        GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);

        drawable.setColor(data.getColor());

        holder.cate.setBackgroundDrawable(drawable);

        holder.share.setOnClickListener(view -> {
            Toast.makeText(context, context.getString(R.string.message_share_exam,data.getTitle()), Toast.LENGTH_LONG).show();

            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");

                intent.putExtra(Intent.EXTRA_TEXT, data.testToString(context));
                context.startActivity(intent);

            } catch (Exception e) {
                Log.d("tag", "Error");
            }
        });

    }

    @Override
    public boolean isAssignable(Object object) {
        return object instanceof Test;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView num;

        ImageTextButton play;
        ImageTextButton edit;
        ImageTextButton delete;
        ImageTextButton share;

        ImageButton cate;

         ViewHolder(View v) {
            super(v);

            num = v.findViewById(R.id.num_questions);
            title = v.findViewById(R.id.title_questions);
            play = v.findViewById(R.id.play);
            edit = v.findViewById(R.id.edit);
            delete =  v.findViewById(R.id.delete);
            share = v.findViewById(R.id.open);
            cate =  v.findViewById(R.id.cate);

        }
    }
}
