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

    private var selectedCategory = ""
        set(value) {
            field = value
            refresh()
            requestModelBuild()
        }

    var categories: List<Cate> = emptyList()
        set(value) {
            field = value
            refresh()
            requestModelBuild()
        }

    var tests: List<Test> = emptyList()
        set(value) {
            field = value
            refresh()
            requestModelBuild()
        }

    private fun refresh() {
        if (tests.isEmpty()) {
            nonCategorizedTests = emptyList()
            return
        }

        nonCategorizedTests = if (categories.isEmpty()) {
            tests
        } else {
            tests.filter { !categories.map { it.category }.contains(it.getCategory()) }
        }

        categorizedTests = tests.filter { it.getCategory() == selectedCategory }
    }

    private var nonCategorizedTests: List<Test> = emptyList()

    private var categorizedTests: List<Test> = emptyList()

    interface OnClickListener {
        fun onClickPlayTest(id: Long)
        fun onClickEditTest(id: Long)
        fun onClickDeleteTest(id: Long)
        fun onClickShareTest(id: Long)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        categories.forEachIndexed { index, it ->
            cardCategory {
                id(index)
                colorId(it.color)
                category(it.category)
                size(context.getString(R.string.number_exams, tests.filter { test -> it.category == test.getCategory() }.size))
                onClick { _, _, _, _ ->
                    selectedCategory = if (selectedCategory == it.category) "" else it.category
                }
                selected(categorizedTests.isNotEmpty() && categorizedTests.first().getCategory() == it.category)

            }

            if (categorizedTests.isNotEmpty() && categorizedTests.first().getCategory() == it.category) {

                categorizedTests.forEach {
                    cardTest {
                        isCategorized(true)
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

        nonCategorizedTests.forEach {
            cardTest {
                isCategorized(false)
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