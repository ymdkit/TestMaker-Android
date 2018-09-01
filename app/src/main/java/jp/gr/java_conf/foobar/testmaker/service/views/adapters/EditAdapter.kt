package jp.gr.java_conf.foobar.testmaker.service.views.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.RealmController
import jp.gr.java_conf.foobar.testmaker.service.views.ImageTextButton

/**
 * Created by keita on 2016/05/29.
 */
class EditAdapter(private val context: Context, private val realmController: RealmController, private val testId: Long) : RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var filter: Boolean = false
    var searchWord: String = ""

    private var listener: EditAdapter.OnClickListener? = null

    interface OnClickListener {
        fun onClickEditQuestion(position: Int)
        fun onClickDeleteQuestion(data: Quest)
    }

    fun setOnClickListener(listener: EditAdapter.OnClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_problem, parent, false))
    }

    override fun getItemCount(): Int {

        return if (filter) realmController.getFilterQuestions(testId, searchWord).size
        else realmController.getQuestions(testId).size

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = init(position)

        holder.problem.text = context.getString(R.string.question, data.problem.toString())
        holder.answer.text = context.getString(R.string.answer, data.answer.toString())

        holder.edit.setOnClickListener {
            if (listener != null) listener?.onClickEditQuestion(position)
        }

        holder.delete.setOnClickListener {
            if (listener != null) listener?.onClickDeleteQuestion(data)
        }

    }

    private fun init(position: Int): Quest {

        return if (filter) realmController.getFilterQuestions(testId, searchWord)[position]
        else realmController.getQuestions(testId)[position]

    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        val problem: TextView = v.findViewById(R.id.problem)
        val answer: TextView = v.findViewById(R.id.answer)
        val edit: ImageTextButton = v.findViewById(R.id.edit)
        val delete: ImageTextButton = v.findViewById(R.id.delete)

    }

}




