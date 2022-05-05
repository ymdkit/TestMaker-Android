package jp.gr.java_conf.foobar.testmaker.service.view.group

import android.content.Context
import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.empty
import jp.gr.java_conf.foobar.testmaker.service.group
import jp.gr.java_conf.foobar.testmaker.service.sectionHeader

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

        if (groups.isEmpty()) {
            empty {
                id("empty")
                message(this@GroupListController.context.getString(R.string.empty_group))
            }
            return
        }

        sectionHeader {
            id("Group")
            title(this@GroupListController.context.getString(R.string.group))
        }

        groups.forEach {

            group {
                id(it.id)
                group(it)
                listener(this@GroupListController.listener)
            }
        }
    }

}