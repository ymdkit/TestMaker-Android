package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.itemGroup
import jp.gr.java_conf.foobar.testmaker.service.itemSectionHeader

class GroupListController(private val context: Context) : EpoxyController() {

    interface OnClickListener {
        fun onClickGroup(group: Group)
    }

    private var listener: OnClickListener? = null

    fun setOnClickListener(listener: OnClickListener) {
        this.listener = listener
    }

    var groups = emptyList<Group>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        itemSectionHeader {
            id("Group")
            title(context.getString(R.string.group))
        }

        groups.forEach {

            itemGroup {
                id(it.id)
                group(it)
                listener(listener)
            }
        }
    }

}