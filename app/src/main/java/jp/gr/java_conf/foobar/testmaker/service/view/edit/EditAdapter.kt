package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.filteredList
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.view.share.ImageTextButton

/**
 * Created by keita on 2016/05/29.
 */
class EditAdapter(private val context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var questions: ArrayList<Question> = ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var searchWord: String = ""

    private var listener: OnClickListener? = null

    interface OnClickListener {
        fun onClickEditQuestion(question: Question)
        fun onClickDeleteQuestion(question: Question, position: Int)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_question, parent, false))
    }

    override fun getItemCount(): Int {
        return if (searchWord.isEmpty()) questions.size else questions.filteredList(searchWord).size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.index.text = (holder.adapterPosition + 1).toString()

        val data = init(holder.adapterPosition)

        holder.problem.text = context.getString(R.string.question, data.question)
        holder.answer.text = context.getString(R.string.answer, data.answer)

        holder.edit.setOnClickListener {
            if (listener != null) listener?.onClickEditQuestion(data)
        }

        holder.delete.setOnClickListener {
            if (listener != null) listener?.onClickDeleteQuestion(data, holder.adapterPosition)
        }

    }

    private fun init(position: Int): Question {
        return if (searchWord.isEmpty()) questions[position] else questions.filteredList(searchWord)[position]
    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        var index: TextView = v.findViewById(R.id.order)
        val problem: TextView = v.findViewById(R.id.problem)
        val answer: TextView = v.findViewById(R.id.answer)
        val edit: ImageTextButton = v.findViewById(R.id.edit)
        val delete: ImageTextButton = v.findViewById(R.id.delete)

    }

}




