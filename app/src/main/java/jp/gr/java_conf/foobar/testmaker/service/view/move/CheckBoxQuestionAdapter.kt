package jp.gr.java_conf.foobar.testmaker.service.view.move

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest

class CheckBoxQuestionAdapter(context: Context, private val array: Array<Quest>): androidx.recyclerview.widget.RecyclerView.Adapter<CheckBoxQuestionAdapter.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    var checkBoxStates: Array<Boolean> = Array(array.size){false}

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.list_check_box_question, parent, false))
    }

    override fun getItemCount(): Int {
        return array.size
    }

    fun getItems(): Array<Quest>{
        return array
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val checkBox = holder.checkBox
        checkBox.isChecked = checkBoxStates[holder.adapterPosition]

        holder.problem.text = array[holder.adapterPosition].problem

        holder.answer.text = array[holder.adapterPosition].answer

        holder.itemView.setOnClickListener {

            checkBox.isChecked = !checkBox.isChecked
            checkBoxStates[holder.adapterPosition] = checkBox.isChecked

        }

        checkBox.setOnClickListener {
            checkBoxStates[position] = checkBox.isChecked
        }

    }

    class ViewHolder(v: View): androidx.recyclerview.widget.RecyclerView.ViewHolder(v) {

        val checkBox = v.findViewById<CheckBox>(R.id.check_move)
        val problem = v.findViewById<TextView>(R.id.problem)
        val answer = v.findViewById<TextView>(R.id.answer)
    }


}