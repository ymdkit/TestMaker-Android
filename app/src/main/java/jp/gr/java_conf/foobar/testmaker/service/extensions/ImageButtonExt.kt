package jp.gr.java_conf.foobar.testmaker.service.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.StorageReference
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.modules.GlideApp

fun ImageButton.setImageWithGlide(context: Context, img: Bitmap){

    val circularProgressDrawable = androidx.swiperefreshlayout.widget.CircularProgressDrawable(context)
    circularProgressDrawable.setColorSchemeColors(Color.WHITE)
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    Glide.with(context).load(img)
        .placeholder(circularProgressDrawable).into(this)

}

fun ImageButton.setImageWithGlide(context: Context, ref: StorageReference){

    val circularProgressDrawable = androidx.swiperefreshlayout.widget.CircularProgressDrawable(context)
    circularProgressDrawable.setColorSchemeColors(ContextCompat.getColor(context, R.color.blue_holo))
    circularProgressDrawable.strokeWidth = 5f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()

    GlideApp.with(context)
            .load(ref)
            .placeholder(circularProgressDrawable)
            .into(this)

}