package jp.gr.java_conf.foobar.testmaker.service.views.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController;

/**
 * Created by keita on 2016/05/29.
 */
public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ViewHolder> {
    private LayoutInflater mLayoutInflater;
    private RealmController mRealmController;
    private long testId;


    public ResultAdapter(Context context, RealmController realm, long testId) {
        super();
        mLayoutInflater = LayoutInflater.from(context);
        mRealmController=realm;

        this.testId=testId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1
        View v = mLayoutInflater.inflate(R.layout.card_result, parent, false);
        return new ViewHolder(v);
    }

    // 4
    @Override
    public int getItemCount() {
        return mRealmController.getQuestionsSolved(testId).size();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // 3
        final Quest data = mRealmController.getQuestionsSolved(testId).get(position);

        holder.number.setText(String.valueOf(position + 1) + ":");
        holder.problem.setText( String.valueOf(data.getProblem()));
        holder.answer.setText( String.valueOf(data.getAnswer()));

        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();

        if (data.getCorrect()) {

            if (lang.equals("ja")) {
                holder.mark.setImageResource(R.drawable.right);
            }else{
                holder.mark.setImageResource(R.drawable.right);
            }

        } else {
            if (lang.equals("ja")) {
                holder.mark.setImageResource(R.drawable.mistake);
            }else{
                holder.mark.setImageResource(R.drawable.mistake);
            }
        }

    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView number;
        TextView problem;
        TextView answer;
        ImageView mark;

        ViewHolder(View v) {
            super(v);
            number = v.findViewById(R.id.number);
            problem = v.findViewById(R.id.problem);
            answer = v.findViewById(R.id.answer);
            mark = v.findViewById(R.id.mark);
        }
    }

}




