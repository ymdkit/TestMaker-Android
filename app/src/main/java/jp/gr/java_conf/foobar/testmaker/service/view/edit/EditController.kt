package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.itemEmpty
import jp.gr.java_conf.foobar.testmaker.service.itemQuestion

class EditController(private val context: Context) : EpoxyController() {

    private var listener: OnClickListener? = null

    var questions: List<Question> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    var searchWord: String = ""
        set(value) {
            field = value
            requestModelBuild()
        }

    interface OnClickListener {
        fun onClickQuestion(question: Question)
    }

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    override fun buildModels() {

        if (questions.isEmpty()) {
            itemEmpty {
                id("empty")
                message(context.getString(R.string.empty_question))
            }

        } else {
            val result =
                if (searchWord.isNotEmpty())
                    questions.filter {
                        it.question.contains(searchWord) || it.answer.contains(
                            searchWord
                        ) || it.answers.any { it.contains(searchWord) }
                    }
                else questions
            result.forEachIndexed { index, it ->
                itemQuestion {
                    id(it.id)
                    index((index + 1).toString())
                    question(it)
                    listener(listener)
                }
            }
        }
    }
}