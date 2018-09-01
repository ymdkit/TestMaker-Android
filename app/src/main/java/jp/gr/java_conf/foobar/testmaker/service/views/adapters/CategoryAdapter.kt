package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController

/**
 * Created by keita on 2016/06/19.
 */
class CategoryAdapter(private val context: Context, private val mRealmController: RealmController, private val mainAdapter: ScrambleAdapter<*>?) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClickCategory(position: Int)
    }

    fun setOnClickListener(listener: CategoryAdapter.OnClickListener) {
        this.listener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(layoutInflater.inflate(R.layout.list_cate, parent, false))
    }

    // 4
    override fun getItemCount(): Int {
        return mRealmController.cateList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = mRealmController.cateList[position]

        holder.cate.text = data.category

        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle, null) as GradientDrawable

        drawable.setColor(data.color)

        holder.color.background = drawable


        holder.itemView.setOnClickListener {

            if (listener != null) listener?.onClickCategory(position)

        }

        holder.delete.setOnClickListener {

            mRealmController.deleteCate(data)

            notifyDataSetChanged()

            mainAdapter?.notifyDataSetChanged()

        }

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var cate: TextView = v.findViewById(R.id.cate)
        var color: ImageView = v.findViewById(R.id.color)
        var delete: ImageButton = v.findViewById(R.id.delete)

    }
}
