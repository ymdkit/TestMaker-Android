package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import jp.gr.java_conf.foobar.testmaker.service.SharedPreferenceManager;

/**
 * Created by keita on 2016/05/10.
 */
public class SePlayer {

    private SoundPool soundPool;

    private int se;// 読み込んだ効果音

    private SharedPreferenceManager sharedPreferenceManager;

    public SePlayer(Context context, int id) {
        // new SoundPool(読み込むファイル数,読み込む種類,読み込む質)
        this.soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

        // load(コンテキスト,読み込むリソースID,音の優先度)
        this.se = soundPool.load(context, id, 1);

        sharedPreferenceManager = new SharedPreferenceManager(context);

    }

    public void playSe() {
        // play(再生するサウンドID,左のボリューム,右のボリューム,優先度,ループ回数(0はしない、-1は無限),再生レート)
        if (!sharedPreferenceManager.getAudio()) {
            soundPool.play(se, 1.0f, 1.0f, 1, 0, 1.0f);
        }
    }
}
