package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.content.Context
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.TypedEpoxyController
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import jp.gr.java_conf.foobar.testmaker.service.*
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class ResultController(private val context: Context, private val listener: OnClickQuestionListener) : TypedEpoxyController<List<Question>>() {

    interface OnClickQuestionListener {
        fun onClickQuestion(question: Question)
        fun onClickHome()
    }

    override fun buildModels(questions: List<Question>) {

        itemSection {
            id("header")
            text(context.getString(R.string.label_result))
        }

        val result: Float = questions.count { it.isCorrect }.toFloat() / questions.size.toFloat()

        itemPieChart {
            id("pie_chart")
            data(PieData(PieDataSet(listOf(PieEntry(result * 100f, "正解"), PieEntry((1 - result) * 100f, "不正解")), "").apply {
                setDrawValues(false)
                colors = listOf(ContextCompat.getColor(context, R.color.colorAccent), ContextCompat.getColor(context, R.color.colorPrimary))
            }))
            centerText("${questions.count { it.isCorrect }}/${questions.size}")
        }

        itemResultMenu {
            id("home")
            text(context.getString(R.string.home))
            icon(ContextCompat.getDrawable(context, R.drawable.ic_home))
            onClick { _ -> listener.onClickHome() }
        }

        itemResultMenu {
            id("retry")
            text(context.getString(R.string.retry))
            icon(ContextCompat.getDrawable(context, R.drawable.ic_refresh_white))
            onClick { _ -> listener.onClickHome() }
        }

        itemSection {
            id("header_questions")
            text(context.getString(R.string.label_result_questions))
        }

        questions.forEachIndexed { index, it ->
            cardResult {
                id(it.id)
                index(index)
                question(it)
                onClick { _ ->
                    listener.onClickQuestion(it)
                }
            }
        }
    }
}