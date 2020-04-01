package jp.gr.java_conf.foobar.testmaker.service

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat.animate
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
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
    fun setCustomAnimatedVisibility(view: View, isVisible: Boolean, duration: Int) {
        if (isVisible) {
            view.visibility = View.VISIBLE
            animate(view).alpha(1f).setDuration(duration.toLong())
        } else {
            animate(view).alpha(0f).setDuration(duration.toLong()).withEndAction { view.visibility = View.GONE }
        }
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

    @BindingAdapter("android:srcPlayWithGlide")
    @JvmStatic
    fun ImageView.setSrcPlayWithGlide(src: String) {
        if (src.isEmpty()) {
            setImageBitmap(null)
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

    @BindingAdapter("android:syncKeyBoard")
    @JvmStatic
    fun EditText.setSyncKeyBoard(isSync: Boolean) {
        if (!isSync) return
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED)
            } else {
                inputMethodManager.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    @BindingAdapter("android:debounceClick")
    @JvmStatic
    fun Button.debounceClick(execute: () -> Unit) {
        setOnClickListener {
            isEnabled = false
            execute()
            Handler().postDelayed({ isEnabled = true }, 600)
        }
    }

    @BindingAdapter("android:data")
    @JvmStatic
    fun PieChart.setData(data: PieData) {
        this.data = data
        animateXY(500, 500)
        description.isEnabled = false
        setCenterTextSize(24f)
        setCenterTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        setDrawEntryLabels(false)
        legend.formSize = 14f
    }

    @BindingAdapter("android:centerText")
    @JvmStatic
    fun PieChart.setCenterText(text: String) {
        centerText = text
    }
}