package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardCategory
import jp.gr.java_conf.foobar.testmaker.service.cardTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Cate
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class MainController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var tests: List<Test> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var categories: List<Cate> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickPlayTest(id: Long)
        fun onClickEditTest(id: Long)
        fun onClickDeleteTest(id: Long)
        fun onClickShareTest(id: Long)
        fun onClickOpen(category: String)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        categories.forEach {
            cardCategory {
                id(it.category)
                colorId(it.color)
                category(it.category)
                listener(listener)

            }
        }

        tests.forEach {
            cardTest {
                id(it.id)
                testId(it.id)
                colorId(it.color)
                title(it.title)
                size(context.getString(R.string.number_existing_questions, it.questionsCorrectCount, it.questionsNonNull().size))
                listener(listener)
            }
        }


    }

}