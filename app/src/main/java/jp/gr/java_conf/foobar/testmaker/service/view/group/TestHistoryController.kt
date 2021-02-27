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
        return adapter.getModelAtPosition(position)::class == ItemSectionHeaderBindingModel_::class
    }

    override fun buildModels() {

        if (histories.isEmpty()) {
            itemEmpty {
                id("empty")
                message(context.getString(R.string.empty_histories))
            }
            return
        }

        itemSectionHeader {
            id("History")
            title(context.getString(R.string.section_history))
        }

        histories.forEach {

            itemHistory {
                id(it.id)
                history(it)
            }
        }
    }

}