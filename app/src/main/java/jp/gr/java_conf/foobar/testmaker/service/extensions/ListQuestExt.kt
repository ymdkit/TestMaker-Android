package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.domain.Quest

fun ArrayList<Quest>.filteredList(searchWord: String): ArrayList<Quest> {
    return ArrayList(filter { it.problem.contains(searchWord) || it.answer.contains(searchWord) || it.answers.any { it.selection.contains(searchWord) } })
}