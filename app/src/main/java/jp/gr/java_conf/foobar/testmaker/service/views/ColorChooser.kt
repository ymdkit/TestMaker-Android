package jp.gr.java_conf.foobar.testmaker.service.views

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.app.AlertDialog
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.R

/**
 * Created by keita on 2018/06/08.
 */

class ColorChooser(internal var context: Context, attr: AttributeSet) : LinearLayout(context, attr) {

    private var colorImages = arrayOfNulls<ImageView>(8)

    private var colorId: Int = 0

    private var dialog: AlertDialog? = null

    internal lateinit var button: ImageButton

    private var colorIds = intArrayOf(R.color.red, R.color.orange, R.color.yellow, R.color.green,
            R.color.dark_green, R.color.blue, R.color.navy, R.color.purple)

    init {

        val layout = LayoutInflater.from(context).inflate(R.layout.color_chooser, this)

        for (i in colorImages.indices) {

            val s = "imageView" + (i + 1).toString()
            val strId = resources.getIdentifier(s, "id", context.packageName)

            colorImages[i] = layout.findViewById(strId)

            val bgShape = colorImages[i]!!.background as GradientDrawable
            bgShape.setColor(ContextCompat.getColor(context,colorIds[i]))

            colorImages[i]!!.tag = i
            colorImages[i]!!.setOnClickListener { v ->

                for (color in colorImages) {
                    color!!.setImageResource(R.drawable.white)
                }

                colorImages[v.tag as Int]!!.setImageResource(R.drawable.ic_done_white)

                colorId = getColors()!![v.tag as Int]

                if (dialog != null) {

                    val drawable = ResourcesCompat.getDrawable(resources,R.drawable.circle,null) as GradientDrawable

                    drawable.setColor(getColors()!![v.tag as Int])

                    button.background = drawable

                    dialog!!.dismiss()
                }
            }

            colorImages[0]!!.setImageResource(R.drawable.ic_done_white)
            colorId = ContextCompat.getColor(context,colorIds[0])
            dialog = null

        }

    }

    fun setColorId(id: Int) {

        colorId = id

        for (i in colorImages.indices) {
            if (id == ContextCompat.getColor(context,colorIds[i])) {
                colorImages[i]!!.setImageResource(R.drawable.ic_done_white)
            } else {
                colorImages[i]!!.setImageResource(R.drawable.white)
            }

        }


    }

    fun setDialog(dialog: AlertDialog, button: ImageButton) {
        this.dialog = dialog
        this.button = button
    }

    fun getColorId(): Int {

        return colorId
    }

    fun getColors(): IntArray? {
        val colorList: IntArray
        val colors = resources.obtainTypedArray(R.array.color_list)

        if (colors.length() <= 0) { return null }

        // リソースID用の配列を準備
        colorList = IntArray(colors.length())
        for (i in 0 until colors.length()) {
            // TypedArrayから指定indexのTypedValueを取得する
            val colorValue = TypedValue()
            if (colors.getValue(i, colorValue)) {
                // TypedValueからリソースIDを取得する
                colorList[i] = ContextCompat.getColor(context,colorValue.resourceId)
            }
        }

        colors.recycle()

        return colorList
    }
}
