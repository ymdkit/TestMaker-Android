package jp.gr.java_conf.foobar.testmaker.service.view.result

import com.airbnb.epoxy.TypedEpoxyController
import jp.gr.java_conf.foobar.testmaker.service.cardResult
import jp.gr.java_conf.foobar.testmaker.service.domain.Question

class ResultController : TypedEpoxyController<List<Question>>() {

    override fun buildModels(questions: List<Question>) {

        questions.forEachIndexed { index, it ->
            cardResult {
                id(it.id)
                index(index)
                question(it)
            }
        }
    }
}