package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool

import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import android.media.AudioAttributes
import android.os.Build


/**
 * Created by keita on 2016/05/10.
 */
class SePlayer(context: Context, id: Int) {

    private val soundPool: SoundPool

    private val se: Int// 読み込んだ効果音

    private val sharedPreferenceManager: SharedPreferenceManager

    init {
        // new SoundPool(読み込むファイル数,読み込む種類,読み込む質)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){

            val attr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()

            this.soundPool = SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(1)
                    .build()
        }else{

            this.soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 0)

        }


        // load(コンテキスト,読み込むリソースID,音の優先度)
        this.se = soundPool.load(context, id, 1)

        sharedPreferenceManager = SharedPreferenceManager(context)

    }

    fun playSe() {
        // play(再生するサウンドID,左のボリューム,右のボリューム,優先度,ループ回数(0はしない、-1は無限),再生レート)
        if (!sharedPreferenceManager.audio) {
            soundPool.play(se, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }
}
