package jp.gr.java_conf.foobar.testmaker.service.domain;

import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by keita on 2016/06/19.
 */
public class Cate extends RealmObject {

    @Required
    private String category;
    private int color;

    public void setCategory(String c) {
        category = c;
    }

    public String getCategory() {
        return category;
    }

    public void setColor(int c) {
        color = c;
    }

    public int getColor() {
        return color;
    }
    
}
