package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import android.widget.ImageButton
import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageButton.setImageWithGlide(context: Context, img: Bitmap){

    scaleType = ImageView.ScaleType.CENTER_CROP

    val circularProgressDrawable = androidx.swiperefreshlayout.widget.CircularProgressDrawable(context)
    circularProgressDrawable.setColorSchemeColors(Color.WHITE)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(context).load(img)
        .placeholder(circularProgressDrawable).into(this)

}