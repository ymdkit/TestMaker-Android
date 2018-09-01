package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter
import net.cattaka.android.adaptertoolbox.adapter.listener.ForwardingListener

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Cate
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

/**
 * Created by keita on 2017/05/21.
 */

class FolderAdapter(private val context: Context, private val realmController: RealmController) : ScrambleAdapter.AbsViewHolderFactory<FolderAdapter.ViewHolder>() {

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClick(category: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(adapter: ScrambleAdapter<*>, parent: ViewGroup, forwardingListener: ForwardingListener<ScrambleAdapter<*>, RecyclerView.ViewHolder>): FolderAdapter.ViewHolder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(adapter: ScrambleAdapter<*>, holder: ViewHolder,
                                  position: Int, `object`: Any?) {

        val data = adapter.getItemAt(position) as Cate

        holder.title.text = data.category

        holder.num.text = context.getString(R.string.number_exams, realmController.getCategorizedList(data.category).size)

        val drawable = ResourcesCompat.getDrawable(context.resources,R.drawable.circle,null) as GradientDrawable

        drawable.setColor(data.color)

        holder.cate.background = drawable

        holder.open.setOnClickListener {

            if (listener != null) { listener?.onClick(data.category) }

        }

    }

    override fun isAssignable(`object`: Any): Boolean {
        return `object` is Cate
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var title: TextView = v.findViewById(R.id.title_questions)
        var num: TextView = v.findViewById(R.id.num_questions)
        var open: ImageTextButton = v.findViewById(R.id.open)
        var cate: ImageButton = v.findViewById(R.id.cate)

    }
}
