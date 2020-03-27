package jp.gr.java_conf.foobar.testmaker.service

import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide

object CustomBindingAdapter {

    @BindingAdapter("android:testColor")
    @JvmStatic
    fun ImageButton.setTestColor(colorId: Int) {
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable
        drawable.setColor(colorId)
        background = drawable
    }

    @BindingAdapter("android:circleTint")
    @JvmStatic
    fun ImageView.setCircleTint(color: Int) {
        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.shape_circle, null) as GradientDrawable
        drawable.setColor(color)
        background = drawable
    }

    @BindingAdapter("android:isHasFixedSize")
    @JvmStatic
    fun RecyclerView.setIsHasFixedSize(isFixed: Boolean) {
        setHasFixedSize(isFixed)
    }

    @BindingAdapter("android:isVisible")
    @JvmStatic
    fun View.setIsVisible(isVisible: Boolean) {
        this.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:onLongClick")
    @JvmStatic
    fun setOnLongClick(view: View, clickListener: View.OnLongClickListener?) {
        view.setOnLongClickListener(clickListener)
    }

    @BindingAdapter("android:animatedVisibility")
    @JvmStatic
    fun setAnimatedVisibility(view: View, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view.rootView as ViewGroup)
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:customAnimatedVisibility", "android:duration")
    @JvmStatic
    fun setCustomAnimatedVisibility(view: View, isVisible: Boolean, duration: Int = 500) {
        TransitionManager.beginDelayedTransition(view.rootView as ViewGroup, AutoTransition().apply { this.duration = duration.toLong() })
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:srcWithGlide")
    @JvmStatic
    fun ImageView.setSrcWithGlide(src: String) {
        if (src.isEmpty()) {
            setImageResource(R.drawable.ic_insert_photo_white_24dp)
        } else {
            if (src.contains("/")) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(src)
                setImageWithGlide(context, storageRef)
            } else {
                setImageWithGlide(context, context.getFileStreamPath(src))
            }
        }
    }
}