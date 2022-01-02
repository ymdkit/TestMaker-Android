package jp.gr.java_conf.foobar.testmaker.service

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager

object CustomBindingAdapter {

    @BindingAdapter("android:isHasFixedSize")
    @JvmStatic
    fun RecyclerView.setIsHasFixedSize(isFixed: Boolean) {
        setHasFixedSize(isFixed)
    }

    @BindingAdapter("android:animatedVisibility")
    @JvmStatic
    fun setAnimatedVisibility(view: View, isVisible: Boolean) {
        TransitionManager.beginDelayedTransition(view.rootView as ViewGroup)
        view.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @BindingAdapter("android:srcResource")
    @JvmStatic
    fun ImageView.setSrcResource(resourceId: Int) {
        setImageResource(resourceId)
    }

    @BindingAdapter("android:tintARGB")
    @JvmStatic
    fun ImageView.setTintARGB(color: Int) {
        setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
    }
}