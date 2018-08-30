package jp.gr.java_conf.foobar.testmaker.service;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {

    private SharedPreferences sharedPreferences;

    public SharedPreferenceManager(Context context) {

        sharedPreferences = context.getSharedPreferences("question", Context.MODE_PRIVATE);

    }

    public int getNumChoose() {
        return sharedPreferences.getInt("num_choose", 3);
    }

    public void setNumChoose(int num) {
        sharedPreferences.edit().putInt("num_choose", num)
                .apply();
    }

    public void setNumWrite(int num) {
        sharedPreferences.edit().putInt("num_write", num)
                .apply();
    }

    public int getNumWrite() {

        return sharedPreferences.getInt("num_write", 1);
    }

    public void setAuto(boolean num) {
        sharedPreferences.edit().putBoolean("auto", num)
                .apply();
    }

    public boolean isAuto() {
        return sharedPreferences.getBoolean("auto", false);
    }

    public void setExplanation(boolean f) {
        sharedPreferences.edit().putBoolean("explanation", f)
                .apply();
    }

    public boolean isExplanation() {
        return sharedPreferences.getBoolean("explanation", false);
    }

    public void setManual(boolean f) {
        sharedPreferences.edit().putBoolean("manual", f)
                .apply();
    }

    public boolean isManual() {
        return sharedPreferences.getBoolean("manual", false);
    }

    public void setRefine(boolean f) {
        sharedPreferences.edit().putBoolean("refine", f)
                .apply();
    }

    public boolean isRefine() {
        return sharedPreferences.getBoolean("refine", false);
    }

    public void setAudio(boolean f) {
        sharedPreferences.edit().putBoolean("audio", f)
                .apply();
    }

    public boolean isAudio() {
        return sharedPreferences.getBoolean("audio", false);
    }

    public void setReverse(boolean f) {
        sharedPreferences.edit().putBoolean("reverse", f)
                .apply();
    }

    public boolean isReverse() {
        return sharedPreferences.getBoolean("reverse", false);
    }


    public void setSort(int i) {
        sharedPreferences.edit().putInt("sort", i).apply();
    }

    public int getSort() {
        return sharedPreferences.getInt("sort", -1);
    }

}
