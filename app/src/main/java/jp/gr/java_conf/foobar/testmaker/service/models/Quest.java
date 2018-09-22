package jp.gr.java_conf.foobar.testmaker.service.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by keita on 2017/02/08.
 */
public class Quest extends RealmObject {

    @PrimaryKey
    private long id;
    private String problem;
    private String answer;
    @Required
    private String explanation;
    private boolean correct;
    private String imagePath;
    private RealmList<Select> selections;
    private RealmList<Select> answers;
    private int type;
    private boolean auto;
    private boolean solving;

    public long getId() {
        return id;
    }

    public void setProblem(String p) {
        problem = p;
    }

    public String getProblem() {
        return problem;
    }

    public String getProblem(boolean isReverse){

        if(isReverse) return answer;

        return problem;

    }

    public void setAnswer(String a) {
        answer = a;
    }

    public String getAnswer() {
        return answer;
    }

    public String getAnswer(boolean isReverse){

        if(isReverse) return problem;

        return answer;

    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String s) {
        explanation = s;
    }

    public void setImagePath(String i) {
        imagePath = i;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setType(int t) {
        type = t;
    }

    public int getType() {
        return type;
    }

    public void setAuto(boolean a) {
        auto = a;
    }

    public boolean getAuto() {
        return auto;
    }

    public void setCorrect(boolean c) {
        correct = c;
    }

    public boolean getCorrect() {
        return correct;
    }

    public void setSolving(boolean c) {
        solving = c;
    }

    public boolean getSolving() {
        return solving;
    }

    public void setSelections(String[] strings) {

        selections.clear();

        for (int i = 0; i < strings.length; i++) {
            Select select = new Select();
            select.setSelection(strings[i]);
            selections.add(select);
        }

    }

    public RealmList<Select> getSelections() {
        return selections;
    }

    public void setAnswers(String[] strings) {

        answers.clear();

        for (int i = 0; i < strings.length; i++) {
            Select select = new Select();
            select.setSelection(strings[i]);
            answers.add(select);
        }

    }

    public RealmList<Select> getAnswers() {
        return answers;
    }



}
