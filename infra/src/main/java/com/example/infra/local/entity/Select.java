package com.example.infra.local.entity;

import io.realm.RealmObject;

/**
 * Created by keita on 2017/02/08.
 */
public class Select extends RealmObject {

    private String select;

    public void setSelection(String s) {
        select = s;
    }

    public String getSelection() {
        return select;
    }

}
