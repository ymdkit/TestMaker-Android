package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.*
import jp.gr.java_conf.foobar.testmaker.service.domain.History

class TestHistoryController(private val context: Context) : EpoxyController() {

    var histories: List<History> = emptyList()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun isStickyHeader(position: Int): Boolean {
        return adapter.getModelAtPosition(position)::class == SectionHeaderBindingModel_::class
    }

    override fun buildModels() {

        if (histories.isEmpty()) {
            empty {
                id("empty")
                message(this@TestHistoryController.context.getString(R.string.empty_histories))
            }
            return
        }

        sectionHeader {
            id("History")
            title(this@TestHistoryController.context.getString(R.string.section_history))
        }

        histories.forEach {

            history {
                id(it.id)
                history(it)
            }
        }
    }

}