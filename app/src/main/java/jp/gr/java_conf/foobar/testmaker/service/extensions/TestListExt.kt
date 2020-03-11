package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest

fun List<RealmTest>.getTestsWithCategory(category: String): Int {

    return this.count { it.getCategory() == category }

}