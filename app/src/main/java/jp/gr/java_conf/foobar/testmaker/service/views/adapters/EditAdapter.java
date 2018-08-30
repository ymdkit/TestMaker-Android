package jp.gr.java_conf.foobar.testmaker.service.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton;

/**
 * Created by keita on 2016/05/29.
 */
public class EditAdapter extends RecyclerView.Adapter<EditAdapter.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private Context mContext;
    private RealmController mRealmController;
    private long testId;

    public boolean filter;
    public String searchWord;

    public interface OnClickListener {
        void onClickEditQuestion(int position);
        void onClickDeleteQuestion(Quest data);
    }

    @Nullable
    private EditAdapter.OnClickListener listener;
    public void setOnClickListener(EditAdapter.OnClickListener listener) {
        this.listener = listener;
    }


    public EditAdapter(Context context, RealmController realm,long testId) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
        filter = false;
        searchWord = "";

        mRealmController=realm;

        this.testId=testId;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1
        View v = mLayoutInflater.inflate(R.layout.card_problem, parent, false);
        return new ViewHolder(v);
    }

    // 4
    @Override
    public int getItemCount() {

        if(filter){
            return mRealmController.getFilterQuestions(testId,searchWord).size();

        }else{
            return mRealmController.getQuestions(testId).size();

        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // 3

        final Quest data = init(position);

        holder.problem.setText(mContext.getString(R.string.question)+String.valueOf(data.getProblem()));
        holder.answer.setText(mContext.getString(R.string.answer)+String.valueOf(data.getAnswer()));

        holder.edit.setOnClickListener(view -> {

            if (listener != null) {
                listener.onClickEditQuestion(position);
            }

        });

        holder.delete.setOnClickListener(v -> {

            if (listener != null) {
                listener.onClickDeleteQuestion(data);
            }

        });

    }

    private Quest init(int position){

        if(filter){
            return mRealmController.getFilterQuestions(testId,searchWord).get(position);

        }else{
            return mRealmController.getQuestions(testId).get(position);

        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView number;
        TextView problem;
        TextView answer;
        ImageTextButton edit;
        ImageTextButton delete;

         ViewHolder(View v) {

            super(v);
            number= v.findViewById(R.id.number);
            problem = v.findViewById(R.id.problem);
            answer = v.findViewById(R.id.answer);
            delete = v.findViewById(R.id.delete);
            edit= v.findViewById(R.id.edit);

        }
    }

}




