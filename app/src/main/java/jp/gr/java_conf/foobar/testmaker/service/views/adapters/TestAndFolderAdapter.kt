package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Cate
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

/**
 * Created by keita on 2017/05/21.
 */

class TestAndFolderAdapter(private val context: Context,val setValue: () -> Unit) : RecyclerView.Adapter<TestAndFolderAdapter.ViewHolder>() {

    private var listener: OnClickListener? = null

    var tests: ArrayList<Test> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var categories: ArrayList<Cate> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    interface OnClickListener {
        fun onClickPlayTest(id: Long)
        fun onClickEditTest(id: Long)
        fun onClickDeleteTest(id: Long)
        fun onClickShareTest(id: Long)

        fun onClickOpen(category: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestAndFolderAdapter.ViewHolder {

        when (viewType) {
            VIEW_TYPE_TEST -> {
                return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_test, parent, false))
            }
            VIEW_TYPE_FOLDER -> {
                return FolderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_category, parent, false))
            }
            else -> {
                return TestViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_test, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        if (holder is TestViewHolder) {
            val data = tests[position - categories.size]

            val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

            drawable.setColor(data.color)

            holder.cate.background = drawable

            holder.title.text = data.title

            holder.num.text = context.getString(R.string.number_existing_questions, data.questionsCorrectCount, data.getQuestions().size)

            holder.play.setOnClickListener {

               listener?.onClickPlayTest(data.id)

            }

            holder.edit.setOnClickListener {

               listener?.onClickEditTest(data.id)
            }

            holder.delete.setOnClickListener {

               listener?.onClickDeleteTest(data.id)

            }

            holder.share.setOnClickListener {

                listener?.onClickShareTest(data.id)

            }

        } else if (holder is FolderViewHolder) {

            val data = categories[position]

            holder.title.text = data.category

            holder.num.text = context.getString(R.string.number_exams, 10000000)//todo 引数の修正

            val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

            drawable.setColor(data.color)

            holder.cate.background = drawable

            holder.open.setOnClickListener {
                listener?.onClickOpen(data.category)

            }
        }
    }

    override fun getItemCount(): Int {
        return categories.size + tests.size
    }

    override fun getItemViewType(position: Int): Int {
        val obj = (categories + tests)[position]
        if (obj is Test) return VIEW_TYPE_TEST
        if (obj is Cate) return VIEW_TYPE_FOLDER
        return VIEW_TYPE_TEST
    }

    abstract class ViewHolder(v: View) : RecyclerView.ViewHolder(v)

    class TestViewHolder(v: View) : ViewHolder(v) {

        var cate: ImageButton = v.findViewById(R.id.cate)
        var title: TextView = v.findViewById(R.id.title_questions)
        var num: TextView = v.findViewById(R.id.num_questions)
        var play: ImageTextButton = v.findViewById(R.id.play)
        var edit: ImageTextButton = v.findViewById(R.id.edit)
        var delete: ImageTextButton = v.findViewById(R.id.delete)
        var share: ImageTextButton = v.findViewById(R.id.open)

    }

    class FolderViewHolder(v: View) : ViewHolder(v) {

        var title: TextView = v.findViewById(R.id.title_questions)
        var num: TextView = v.findViewById(R.id.num_questions)
        var open: ImageTextButton = v.findViewById(R.id.open)
        var cate: ImageButton = v.findViewById(R.id.cate)

    }

    companion object {
        private const val VIEW_TYPE_TEST = 1
        private const val VIEW_TYPE_FOLDER = 2
    }
}
