package jp.gr.java_conf.foobar.testmaker.service.domain

import io.realm.RealmObject
import io.realm.annotations.Required

/**
 * Created by keita on 2016/06/19.
 */
open class Cate : RealmObject() {
    @Required
    var category: String = ""
    var color = 0
    var order = 0

}