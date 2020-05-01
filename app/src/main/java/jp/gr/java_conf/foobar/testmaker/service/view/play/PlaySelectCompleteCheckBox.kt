package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.content.Context
import android.util.AttributeSet
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.checkbox.MaterialCheckBox

class PlaySelectCompleteCheckBox(context: Context, attributeSet: AttributeSet) : MaterialCheckBox(context, attributeSet) {
    var lastCheckedTime = 0L
}

object PlaySelectCompleteCheckBoxAdapter {
    @BindingAdapter("lastCheckedTime")
    @JvmStatic
    fun setLastCheckedTime(view: PlaySelectCompleteCheckBox, newValue: Long) {
        // Important to break potential infinite loops.
        if (view.lastCheckedTime != newValue) {
            view.lastCheckedTime = newValue
        }
    }

    @BindingAdapter("lastCheckedTimeAttrChanged")
    @JvmStatic
    fun setListener(
            view: PlaySelectCompleteCheckBox,
            attrChange: InverseBindingListener?
    ) {
        view.setOnClickListener {
            view.lastCheckedTime = System.currentTimeMillis()
            attrChange?.onChange()
        }
    }

    @InverseBindingAdapter(attribute = "lastCheckedTime")
    @JvmStatic
    fun getLastCheckedTime(view: PlaySelectCompleteCheckBox): Long {
        return view.lastCheckedTime
    }
}

data class PlaySelectCompleteSelection(
        var content: String = "",
        var lastCheckedTime: Long = 0L,
        var checked: Boolean = false
)

