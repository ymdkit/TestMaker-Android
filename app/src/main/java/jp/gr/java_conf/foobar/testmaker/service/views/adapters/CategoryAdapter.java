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
import android.widget.ImageView;
import android.widget.TextView;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Cate;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;

/**
 * Created by keita on 2016/06/19.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private RealmController mRealmController;
    private ScrambleAdapter mainAdapter;

    public interface OnClickListener{
        void OnClickCategory(int position);
    }

    @Nullable
    private OnClickListener listener;
    public void setOnClickListener(CategoryAdapter.OnClickListener listener) {
        this.listener = listener;
    }


    public CategoryAdapter(Context context, RealmController realm, ScrambleAdapter mainAdapter) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        mRealmController = realm;
        this.mainAdapter=mainAdapter;

    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1
        View v = mLayoutInflater.inflate(R.layout.list_cate, parent, false);
        return new ViewHolder(v);
    }

    // 4
    @Override
    public int getItemCount() {
        return mRealmController.getCateList().size();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        // 3
        final Cate data = mRealmController.getCateList().get(position);

        holder.cate.setText(data.getCategory());

        GradientDrawable drawable = (GradientDrawable) mContext.getResources().getDrawable(R.drawable.circle);

        drawable.setColor(data.getColor());

        holder.color.setBackgroundDrawable(drawable);


        holder.itemView.setOnClickListener(v -> {

            if(listener != null){

                listener.OnClickCategory(position);

            }

//            mListener.onRecyclerClicked(v, position);
//
//            holder.itemView.setClickable(false);
//
//            new Handler().postDelayed(() -> holder.itemView.setClickable(true), 500);
        });

        holder.delete.setOnClickListener(v -> {

            mRealmController.deleteCate(data);
            notifyDataSetChanged();

            if(mainAdapter!=null) {
                mainAdapter.notifyDataSetChanged();
            }

        });

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView cate;
        ImageView color;
        ImageButton delete;

        ViewHolder(View v) {
            super(v);
            // 2
            cate =  v.findViewById(R.id.cate);
            color =  v.findViewById(R.id.color);
            delete =  v.findViewById(R.id.delete);

        }
    }
}
