package jp.gr.java_conf.foobar.testmaker.service.models

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.ColorChooser
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.CategoryAdapter
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.TestAndFolderAdapter

/**
 * Created by keita on 2017/05/20.
 */

class CategoryEditor(private val context: Context, private val buttonCate: Button, private val realmController: RealmController, private val categoryAdapter: TestAndFolderAdapter?) {
    private var buttonColor: ImageButton? = null
    private var colorChooser: ColorChooser? = null

    private var dialogCate: AlertDialog? = null

    fun setCategory() {

        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_category, null)

        val adapter = CategoryAdapter(context, realmController, categoryAdapter)

        adapter.setOnClickListener(object : CategoryAdapter.OnClickListener{
            override fun onClickCategory(position: Int) {
                buttonCate.text = realmController.cateList[position].category

                val drawable = ResourcesCompat.getDrawable(context.resources,R.drawable.circle,null) as GradientDrawable

                drawable.setColor(realmController.cateList[position].color)

                buttonCate.background = drawable
                buttonColor!!.background = context.resources.getDrawable(R.drawable.button_blue)

                buttonCate.tag = realmController.cateList[position].category

                dialogCate!!.dismiss()
            }
        })

        val recyclerView = dialogLayout.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true) // アイテムは固定サイズ
        recyclerView.adapter = adapter

        colorChooser = LayoutInflater.from(context).inflate(R.layout.dialog_color, null).findViewById(R.id.color_chooser)

        buttonColor = dialogLayout.findViewById(R.id.color)
        buttonColor!!.setBackgroundDrawable(ResourcesCompat.getDrawable(context.resources,R.drawable.button_blue,null))
        buttonColor!!.setOnClickListener {

            val layoutColor = LayoutInflater.from(context).inflate(R.layout.dialog_color, null)
            val dialogColor = setDialog(layoutColor, context.getString(R.string.edit_color))

            colorChooser = layoutColor.findViewById(R.id.color_chooser)
            colorChooser!!.setColorId(colorChooser!!.getColors()!![0])
            colorChooser!!.setDialog(dialogColor, buttonColor!!)

            dialogColor.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.GONE
            dialogColor.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE

        }

        val add = dialogLayout.findViewById<ImageButton>(R.id.add)
        add.setOnClickListener {
            val e = dialogLayout.findViewById<EditText>(R.id.set_cate)

            if (e.text.toString() == "") {

                Toast.makeText(context, context.getString(R.string.message_wrong), Toast.LENGTH_SHORT).show()

            } else {

                val cate = e.text.toString()
                buttonCate.tag = cate

                buttonCate.text = cate

                val drawable = ResourcesCompat.getDrawable(context.resources,R.drawable.circle,null) as GradientDrawable

                drawable.setColor(colorChooser!!.getColorId())

                buttonCate.background = drawable
                buttonColor!!.background =ResourcesCompat.getDrawable(context.resources,R.drawable.button_blue,null)

                realmController.addCate(e.text.toString(), colorChooser!!.getColorId())
                adapter.notifyDataSetChanged()

                dialogCate!!.dismiss()

            }


        }

        dialogCate = setDialog(dialogLayout, context.getString(R.string.edit_category))

        dialogCate!!.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.GONE

        val negative = dialogCate!!.getButton(DialogInterface.BUTTON_NEGATIVE)
        negative.setOnClickListener {
            // 場合によっては自分で明示的に閉じる必要がある
            dialogCate!!.dismiss()

        }

    }

    private fun setDialog(dialogLayout: View, title: String): AlertDialog {

        val builder = AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
        builder.setView(dialogLayout)
        builder.setTitle(title)
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setNegativeButton(android.R.string.cancel, null)

        return builder.show()
    }

}
