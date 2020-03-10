package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardCategory
import jp.gr.java_conf.foobar.testmaker.service.cardTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class MainController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    private var selectedCategory = ""
        set(value) {
            field = value
            refresh()
            requestModelBuild()
        }

    var categories: List<Category> = emptyList()
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
            tests.filter { !categories.map { it.name }.contains(it.getCategory()) }
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

        categories.forEach {
            cardCategory {
                id(it.name)
                category(it)
                size(context.getString(R.string.number_exams, tests.filter { test -> it.name == test.getCategory() }.size))
                onClick { _, _, _, _ ->
                    selectedCategory = if (selectedCategory == it.name) "" else it.name
                }
                selected(categorizedTests.isNotEmpty() && categorizedTests.first().getCategory() == it.name)

            }

            if (categorizedTests.isNotEmpty() && categorizedTests.first().getCategory() == it.name) {

                categorizedTests.forEach {
                    cardTest {
                        isCategorized(true)
                        id(it.id)
                        test(it)
                        title(it.title)
                        color(it.color)
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
                test(it)
                title(it.title)
                color(it.color)
                size(context.getString(R.string.number_existing_questions, it.questionsCorrectCount, it.questionsNonNull().size))
                listener(listener)
            }
        }
    }
}