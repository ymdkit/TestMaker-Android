package jp.gr.java_conf.foobar.testmaker.service.view.main

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.cardCategory
import jp.gr.java_conf.foobar.testmaker.service.cardTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Category
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.itemTestEmpty

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
            tests.filter { !categories.map { it.name }.contains(it.category) }
        }

        categorizedTests = tests.filter { it.category == selectedCategory }
    }

    private var nonCategorizedTests: List<Test> = emptyList()

    private var categorizedTests: List<Test> = emptyList()

    interface OnClickListener {
        fun onClickPlayTest(test: Test)
        fun onClickEditTest(test: Test)
        fun onClickDeleteTest(test: Test)
        fun onClickShareTest(test: Test)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        if (tests.isEmpty()) {
            itemTestEmpty {
                id("empty")
            }
            return
        }

        categories.forEach {
            cardCategory {
                id(it.name)
                category(it)
                size(context.getString(R.string.number_exams, tests.filter { test -> it.name == test.category }.size))
                onClick { _, _, _, _ ->
                    selectedCategory = if (selectedCategory == it.name) "" else it.name
                }
                selected(categorizedTests.isNotEmpty() && categorizedTests.first().category == it.name)

            }

            if (categorizedTests.isNotEmpty() && categorizedTests.first().category == it.name) {

                categorizedTests.forEach {
                    cardTest {
                        isCategorized(true)
                        id(it.id)
                        test(it)
                        size(context.getString(R.string.number_existing_questions, it.questionsCorrectCount, it.questions.size))
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
                size(context.getString(R.string.number_existing_questions, it.questionsCorrectCount, it.questions.size))
                listener(listener)
            }
        }
    }
}