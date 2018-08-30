package jp.gr.java_conf.foobar.testmaker.service.views.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;
import net.cattaka.android.adaptertoolbox.adapter.listener.ForwardingListener;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Cate;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton;

/**
 * Created by keita on 2017/05/21.
 */

public class FolderAdapter extends ScrambleAdapter.AbsViewHolderFactory<FolderAdapter.ViewHolder> {

    private RealmController realmController;
    private Context context;

    public interface OnClickListener {
        void onClick(String category);
    }

    @Nullable
    private OnClickListener listener;

    public void setOnClickListener(OnClickListener listener) {
        this.listener = listener;
    }

    public FolderAdapter(Context context, RealmController realm) {
        this.context = context;
        realmController = realm;
    }

    @NonNull
    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(@NonNull ScrambleAdapter<?> adapter, @NonNull ViewGroup parent, @NonNull ForwardingListener<ScrambleAdapter<?>, RecyclerView.ViewHolder> forwardingListener) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScrambleAdapter adapter, @NonNull ViewHolder holder,
                                 final int position, Object object) {

        final Cate data = (Cate) adapter.getItemAt(position);

        holder.title.setText(data.getCategory());

        holder.num.setText(context.getString(R.string.number_exams, realmController.getCategorizedList(data.getCategory()).size()));

        GradientDrawable drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.circle);

        drawable.setColor(data.getColor());

        holder.cate.setBackgroundDrawable(drawable);

        holder.open.setOnClickListener(view -> {

            if(listener != null){
                listener.onClick(data.getCategory());
            }

        });

    }

    @Override
    public boolean isAssignable(Object object) {
        return object instanceof Cate;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView num;

        ImageTextButton open;
        ImageButton cate;

        ViewHolder(View v) {
            super(v);

            num = v.findViewById(R.id.num_questions);
            title = v.findViewById(R.id.title_questions);
            open = v.findViewById(R.id.open);
            cate = v.findViewById(R.id.cate);

        }
    }
}
