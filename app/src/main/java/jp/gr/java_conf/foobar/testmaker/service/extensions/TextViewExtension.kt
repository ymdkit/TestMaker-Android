package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.FontUtil
import jp.gr.java_conf.foobar.testmaker.service.R

fun TextView.setFontSize(key: String) {

    val util = FontUtil()
    // getDimen で受け取る値は px 変換済みのもの， setTextSize で渡すのは sp の値
    when (key) {
        resources.getStringArray(R.array.play_font_size_values)[0] ->
            textSize = util.convertPxToSp(resources.getDimension(R.dimen.text_size_medium), context)
        resources.getStringArray(R.array.play_font_size_values)[1] -> {
            textSize = util.convertPxToSp(resources.getDimension(R.dimen.text_size_large), context)
        }
    }
}