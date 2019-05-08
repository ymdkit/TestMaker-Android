package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.models.Test

fun ArrayList<Test>.getTestsWithCategory(category: String):Int{

    return this.count { it.getCategory() == category }

}