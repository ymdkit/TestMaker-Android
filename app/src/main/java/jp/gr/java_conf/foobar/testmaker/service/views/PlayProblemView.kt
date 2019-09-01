package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import com.google.firebase.storage.StorageReference
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import kotlinx.android.synthetic.main.layout_play_problem.view.*

class PlayProblemView : LinearLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init { LayoutInflater.from(context).inflate(R.layout.layout_play_problem, this) }

    fun setTextProblem(text: String) { problem.text = text }

    fun setTextNumber(text: String) { number.text = text }

    fun hideImage() { layout_image.visibility = View.GONE }

    fun showImage() { layout_image.visibility = View.VISIBLE }

    fun getImageProblem(): ImageButton? {

        return image_problem
    }

    fun setImage(ref: StorageReference){
        image_problem.setImageWithGlide(context,ref)
        image_problem.setBackgroundColor(context.resources.getColor(R.color.white))
    }
}
