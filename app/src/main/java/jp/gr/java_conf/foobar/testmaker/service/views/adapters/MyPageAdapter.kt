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
import com.nifcloud.mbaas.core.NCMBObject
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

class MyPageAdapter(private val context: Context, private val array: MutableList<NCMBObject>) : RecyclerView.Adapter<MyPageAdapter.ViewHolder>()  {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var listener: MyPageAdapter.OnClickListener? = null

    interface OnClickListener {
        fun onClickPlayTest(obj: NCMBObject)
        fun onClickDeleteTest(obj: NCMBObject)
        fun onClickInfoTest(obj: NCMBObject)
    }

    fun setOnClickListener(listener: MyPageAdapter.OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_online_my_page, parent, false))

    }

    override fun getItemCount(): Int {

        return array.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.title.text = array[position].getString("title")

        holder.num.text = context.getString(R.string.number_download,array[position].getInt("downloadedNum"))

        holder.play.setOnClickListener {

            if (listener != null) listener!!.onClickPlayTest(array[position])

        }

        holder.delete.setOnClickListener {

            if (listener != null) listener!!.onClickDeleteTest(array[position])

        }

        holder.information.setOnClickListener{

            if (listener != null) listener!!.onClickInfoTest(array[position])

        }

        val drawable = ResourcesCompat.getDrawable(context.resources, R.drawable.circle,null) as GradientDrawable

        drawable.setColor(array[position].getInt("color"))

        holder.cate.background = drawable

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val cate: ImageButton = v.findViewById(R.id.cate)
        val title: TextView = v.findViewById(R.id.title_questions)
        val num: TextView = v.findViewById(R.id.num_questions)
        val information: ImageTextButton = v.findViewById(R.id.open)
        val play: ImageTextButton = v.findViewById(R.id.play)
        val delete: ImageTextButton = v.findViewById(R.id.delete)

    }

}