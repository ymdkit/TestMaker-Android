package jp.gr.java_conf.foobar.testmaker.service.view.group

import com.airbnb.epoxy.EpoxyController
import jp.gr.java_conf.foobar.testmaker.service.domain.Group
import jp.gr.java_conf.foobar.testmaker.service.itemGroup

class GroupListController : EpoxyController() {

    var groups = emptyList<Group>()
        set(value) {
            field = value
            requestModelBuild()
        }

    override fun buildModels() {

        groups.forEach {

            itemGroup {
                id(it.id)
                group(it)
            }
        }
    }

}