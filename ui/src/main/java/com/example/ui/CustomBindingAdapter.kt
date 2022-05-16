package com.example.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView

object CustomBindingAdapter {

    @BindingAdapter("android:isHasFixedSize")
    @JvmStatic
    fun RecyclerView.setIsHasFixedSize(isFixed: Boolean) {
        setHasFixedSize(isFixed)
    }

    @BindingAdapter("android:srcResource")
    @JvmStatic
    fun ImageView.setSrcResource(resourceId: Int) {
        setImageResource(resourceId)
    }
}