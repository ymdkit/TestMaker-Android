package jp.gr.java_conf.foobar.testmaker.service

import android.content.Context

class FontUtil {
    fun convertSpToPx(sp: Float, context: Context): Float {
        return sp * context.resources.displayMetrics.scaledDensity;
    }

    fun convertPxToSp(px: Float, context: Context): Float {
        return px / context.resources.displayMetrics.scaledDensity;
    }
}