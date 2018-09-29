package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.nifty.cloud.mb.core.NCMBObject
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

class OnlineTestAdapter(private val context: Context, private val array: MutableList<NCMBObject>) : RecyclerView.Adapter<OnlineTestAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var listener: OnlineTestAdapter.OnClickListener? = null

    interface OnClickListener {
        fun onClickPlayTest(content:String)
    }

    fun setOnClickListener(listener: OnlineTestAdapter.OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_test, parent, false))
    }

    override fun getItemCount(): Int {

        return array.size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.title.text = array[position].getString("title")

        holder.play.setOnClickListener {

            if (listener != null) listener!!.onClickPlayTest(array[position].getString("content"))

        }

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val title: TextView = v.findViewById(R.id.title_questions)
        var play: ImageTextButton = v.findViewById(R.id.play)

    }

}