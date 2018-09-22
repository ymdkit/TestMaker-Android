package jp.gr.java_conf.foobar.testmaker.service.models;

import java.io.Serializable;

import jp.gr.java_conf.foobar.testmaker.service.Constants;

/**
 * Created by keita on 2016/06/02.
 */
public class StructQuestion implements Serializable {

    public int type;
    public String answer;
    public String question;
    public String[] others;
    public String[] answers;
    public String imagePath;
    public String explanation;
    public boolean light;
    public boolean auto;

    public StructQuestion(String q, String a) {
        question = q;
        answer = a;
        explanation = "";
        light = false;
        others = new String[3];
        answers = new String[0];
        type = Constants.WRITE;
        auto = false;
    }

    public StructQuestion(String q, String a[]) {
        question = q;
        answer = "";

        for (String answer: a
             ) {
            this.answer += answer + " ";
        }

        explanation = "";

        light = false;
        answers = a;
        others = new String[0];
        type = Constants.COMPLETE;
        auto = false;
    }

    public StructQuestion(String q, String a, String[] o) {
        question = q;
        answer = a;
        others = o;
        answers = new String[0];
        type = Constants.SELECT;
        explanation = "";
        light = false;
        auto = false;

    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setImagePath(String path) {
        this.imagePath = path;
    }

    public void setAuto(boolean auto) {
        this.auto = auto;
    }
}
