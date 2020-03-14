package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

/**
 * Created by keita on 2016/05/29.
 */
class ResultAdapter(context: Context) : androidx.recyclerview.widget.RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var questions: List<Question> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.card_result, parent, false))
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val data = questions[holder.adapterPosition]

        holder.number.text = (holder.adapterPosition + 1).toString()
        holder.problem.text = data.question
        holder.answer.text = data.answer

        holder.mark.setImageResource(if (data.isCorrect) R.drawable.right else R.drawable.mistake)

    }

    class ViewHolder(v: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        val number: TextView = v.findViewById(R.id.number)
        val problem: TextView = v.findViewById(R.id.problem)
        val answer: TextView = v.findViewById(R.id.answer)
        val mark: ImageView = v.findViewById(R.id.mark)

    }

}




