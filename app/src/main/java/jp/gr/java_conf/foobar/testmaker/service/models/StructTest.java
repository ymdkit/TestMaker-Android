package jp.gr.java_conf.foobar.testmaker.service.models;

import java.io.Serializable;
import java.util.ArrayList;

import jp.gr.java_conf.foobar.testmaker.service.R;

/**
 * Created by keita on 2016/05/22.
 */
public class StructTest implements Serializable {

    private String title;
    private int color;
    private String category;
    private long history;

    public ArrayList<StructQuestion> problems;

    StructTest(String title) {
        this.title = title;
        color = R.color.white;
        problems = new ArrayList<>();
        category = "";
        history = 0;
    }

    public void setTitle(String title){this.title =title;}

    public void setColor(int color) {
        this.color = color;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setStructQuestion(String question, String answer, int position) {
        if (problems.size() <= position) {
            problems.add(new StructQuestion(question, answer));
        } else {
            problems.set(position, new StructQuestion(question, answer));
        }
    }

    public void setStructQuestion(String question, String[] answers, int position) {
        if (problems.size() <= position) {
            problems.add(new StructQuestion(question, answers));
        } else {
            problems.set(position, new StructQuestion(question, answers));
        }
    }


    public void setStructQuestion(String question, String answer, String[] others, int position) {
        if (problems.size() <= position) {
            problems.add(new StructQuestion(question, answer, others));
        } else {
            problems.set(position, new StructQuestion(question, answer, others));
        }
    }

    public String getTitle(){
        return title;
    }

    public int getColor(){
        return color;
    }

    public String getCategory(){
        return category;
    }

    public long getHistory(){
        return history;
    }

}
