package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import com.example.usecase.model.QuestionUseCaseModel
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.empty
import jp.gr.java_conf.foobar.testmaker.service.question

class EditController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var questions: List<QuestionUseCaseModel> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var searchWord: String = ""
        set(value) {
            field = value
            requestModelBuild()
        }

    var selectedQuestions = listOf<QuestionUseCaseModel>()
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickQuestion(question: QuestionUseCaseModel)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        if (questions.isEmpty()) {
            empty {
                id("empty")
                message(this@EditController.context.getString(R.string.empty_question))
            }

        } else {
            val result =
                if (searchWord.isNotEmpty())
                    questions.filter {
                        it.problem.contains(searchWord) || it.answers.any { it.contains(searchWord) }
                    }
                else questions
            result.forEachIndexed { index, it ->
                question {
                    id(it.id)
                    questionId(it.id)
                    problem(it.problem)
                    answer(it.getSingleLineAnswer())
                    index((index + 1).toString())
                    onClick { _ ->
                        this@EditController.listener?.onClickQuestion(it)
                    }
                    isSelected(this@EditController.selectedQuestions.any { question -> it.id == question.id })
                }
            }
        }
    }
}