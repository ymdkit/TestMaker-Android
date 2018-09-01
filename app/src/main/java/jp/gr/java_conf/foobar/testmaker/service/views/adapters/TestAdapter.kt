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

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter
import net.cattaka.android.adaptertoolbox.adapter.listener.ForwardingListener

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Test
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

/**
 * Created by keita on 2017/05/21.
 */

class TestAdapter(private val context: Context) : ScrambleAdapter.AbsViewHolderFactory<TestAdapter.ViewHolder>() {

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClickPlayTest(id: Long)
        fun onClickEditTest(id: Long)
        fun onClickDeleteTest(id: Long)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(adapter: ScrambleAdapter<*>, parent: ViewGroup, forwardingListener: ForwardingListener<ScrambleAdapter<*>, RecyclerView.ViewHolder>): TestAdapter.ViewHolder {

        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.card_test, parent, false))
    }

    override fun onBindViewHolder(adapter: ScrambleAdapter<*>, holder: ViewHolder,
                                  position: Int, `object`: Any?) {

        val data = adapter.getItemAt(position) as Test

        holder.title.text = data.title

        holder.num.text = context.getString(R.string.number_existing_questions, data.questionsCorrectCount, data.questions.size)

        holder.play.setOnClickListener {

            if (listener != null) listener!!.onClickPlayTest(data.id)

        }

        holder.edit.setOnClickListener {

            if (listener != null) listener!!.onClickEditTest(data.id)
        }

        holder.delete.setOnClickListener {

            if (listener != null) listener!!.onClickDeleteTest(data.id)

        }

        val drawable = ResourcesCompat.getDrawable(context.resources,R.drawable.circle,null) as GradientDrawable

        drawable.setColor(data.color)

        holder.cate.background = drawable

        holder.share.setOnClickListener {

            Toast.makeText(context, context.getString(R.string.message_share_exam, data.title), Toast.LENGTH_LONG).show()

            try {
                val intent = Intent()
                intent.action = Intent.ACTION_SEND
                intent.type = "text/plain"

                intent.putExtra(Intent.EXTRA_TEXT, data.testToString(context))
                context.startActivity(intent)

            } catch (e: Exception) {
                Log.d("tag", "Error")
            }
        }

    }

    override fun isAssignable(`object`: Any): Boolean {
        return `object` is Test
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var cate: ImageButton = v.findViewById(R.id.cate)
        var title: TextView = v.findViewById(R.id.title_questions)
        var num: TextView = v.findViewById(R.id.num_questions)
        var play: ImageTextButton = v.findViewById(R.id.play)
        var edit: ImageTextButton = v.findViewById(R.id.edit)
        var delete: ImageTextButton = v.findViewById(R.id.delete)
        var share: ImageTextButton = v.findViewById(R.id.open)

    }
}
