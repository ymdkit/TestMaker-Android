package jp.gr.java_conf.foobar.testmaker.service.view.category

import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.view.share.ColorChooser
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestAndFolderAdapter

/**
 * Created by keita on 2017/05/20.
 */

class CategoryEditor(private val context: Context,
                     private val buttonCate: Button,
                     private val categoryAdapter: TestAndFolderAdapter?,
                     private val getCategories: () -> List<Cate>,
                     private val addCategory: (Cate) -> Unit,
                     private val deleteCategory: (Cate) -> Unit) {

    private var colorChooser: ColorChooser? = null

    private var dialogCate: AlertDialog? = null

    fun setCategory() {

        val dialogLayout = LayoutInflater.from(context).inflate(R.layout.dialog_category, null)

        val adapter = CategoryAdapter(context, categoryAdapter)
        adapter.deleteCategory = {
            deleteCategory(it)
            adapter.categories = getCategories()
        }
        adapter.categories = getCategories()

        adapter.setOnClickListener(object : CategoryAdapter.OnClickListener {
            override fun onClickCategory(position: Int) {
                buttonCate.text = getCategories()[position].category

                val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

                drawable.setColor(getCategories()[position].color)

                buttonCate.background = drawable
                buttonCate.tag = getCategories()[position].category

                dialogCate!!.dismiss()
            }
        })

        val recyclerView = dialogLayout.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true) // アイテムは固定サイズ
        recyclerView.adapter = adapter

        colorChooser = dialogLayout.findViewById(R.id.color_chooser)


        val add = dialogLayout.findViewById<ImageButton>(R.id.add)
        add.setOnClickListener {
            val e = dialogLayout.findViewById<EditText>(R.id.set_cate)

            if (e.text.toString() == "") {

                Toast.makeText(context, context.getString(R.string.message_wrong), Toast.LENGTH_SHORT).show()

            } else {

                val cate = e.text.toString()
                buttonCate.tag = cate

                buttonCate.text = cate

                val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

                drawable.setColor(colorChooser!!.getColorId())

                buttonCate.background = drawable
                val category = Cate()
                category.category = e.text.toString()
                category.color = colorChooser!!.getColorId()

                addCategory(category)

                adapter.categories = getCategories()

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
