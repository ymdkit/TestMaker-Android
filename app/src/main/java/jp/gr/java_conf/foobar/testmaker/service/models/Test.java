package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;

import java.util.Calendar;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.Constants;

/**
 * Created by keita on 2017/02/08.
 */

public class Test extends RealmObject {

    @PrimaryKey
    private long id;
    private int color;
    private int limit;
    private String title;
    private String category;

    private long history;

    private RealmList<Quest> questions;

    public long getId() {
        return id;
    }

    public void setTitle(String s) {
        title = s;
    }

    public String getTitle() {
        return title;
    }

    public void setColor(int c) {
        color = c;
    }

    public int getColor() {
        return color;
    }

    public void setHistory() {
        Calendar c = Calendar.getInstance();
        history = c.getTimeInMillis();
    }

    public void setHistory(long l) {

        history = l;
    }

    public void setLimit(int i) {
        limit = i;
    }

    public int getLimit() {
        return limit;
    }


    public long getHistory() {
        return history;
    }

    public void setCategry(String c) {
        category = c;
    }

    public String getCategory() {
        if (category == null) {
            return "";
        }
        return category;
    }

    public RealmList<Quest> getQuestions() {

        if (questions == null) return new RealmList<>();

        return questions;
    }

    public int getQuestionsCorrectCount() {

        if (questions == null) return 0;

        int count = 0;

        for(Quest question: questions){

            if(question.getCorrect()){

                count++;

            }

        }

        return count;
    }

    public void setQuestions(RealmList<Quest> q) {
        questions = q;
    }

    public String testToString(Context context) {

        String backup = "";

        RealmList<Quest> questions = getQuestions();

        for (int i = 0; i < questions.size(); i++) {
            Quest q = questions.get(i);

            StringBuilder write_line = new StringBuilder();

            switch (q.getType()) {
                case Constants.WRITE:
                    write_line = new StringBuilder(context.getString(R.string.share_short_answers, q.getProblem(), q.getAnswer()));

                    break;

                case Constants.COMPLETE:

                    write_line = new StringBuilder(context.getString(R.string.share_multiple_answers, q.getProblem()));

                    for (int k = 0; k < q.getAnswers().size(); k++) {
                        write_line.append(q.getAnswers().get(k).getSelection()).append(",");
                    }
                    write_line = new StringBuilder(write_line.substring(0, write_line.length() - 1));

                    break;
                case Constants.SELECT:
                    if (q.getAuto()) {
                        write_line = new StringBuilder(context.getString(R.string.share_selection_auto_problems, q.getProblem(), q.getAnswer(), q.getSelections().size()));

                    } else {
                        write_line = new StringBuilder(context.getString(R.string.share_selection_problems, q.getProblem(), q.getAnswer()));

                        for (int k = 0; k < q.getSelections().size(); k++) {
                            write_line.append(q.getSelections().get(k).getSelection()).append(",");
                        }
                        write_line = new StringBuilder(write_line.substring(0, write_line.length() - 1));

                    }

                    break;
            }

            if (write_line.toString().contains("\n")) {
                write_line = new StringBuilder(write_line.toString().replaceAll("\n", "<br>"));
            }

            backup += write_line;

            if (!q.getExplanation().equals("")) {
                backup += "\n";
                backup += context.getString(R.string.share_explanation, q.getExplanation());
            }

            backup += "\n";

        }

        backup += context.getString(R.string.share_title, getTitle());
        backup += "\n";
        backup += context.getString(R.string.share_category, getCategory());
        backup += "\n";
        backup += context.getString(R.string.share_color, String.valueOf(getColor()));

        return backup;

    }


}
