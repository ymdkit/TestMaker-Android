package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Cate

/**
 * Created by keita on 2016/06/19.
 */
class CategoryAdapter(private val context: Context, private val mainAdapter: TestAndFolderAdapter?) : androidx.recyclerview.widget.RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    var categories: List<Cate> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }


    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var listener: OnClickListener? = null

    var deleteCategory: (Cate) -> Unit = {}

    interface OnClickListener {
        fun onClickCategory(position: Int)
    }

    fun setOnClickListener(listener: CategoryAdapter.OnClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.list_cate, parent, false))
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = categories[holder.adapterPosition]

        holder.cate.text = data.category

        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

        drawable.setColor(data.color)

        holder.color.background = drawable


        holder.itemView.setOnClickListener {

            if (listener != null) listener?.onClickCategory(position)

        }

        holder.delete.setOnClickListener {

            deleteCategory(data)

            //mRealmController.deleteCate(data)

            notifyDataSetChanged()

            mainAdapter?.also {
                it.setValue()
            }

        }

    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        var cate: TextView = v.findViewById(R.id.text_category)
        var color: ImageView = v.findViewById(R.id.color)
        var delete: ImageButton = v.findViewById(R.id.delete)

    }
}
