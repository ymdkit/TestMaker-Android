package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.domain.Question

fun ArrayList<Question>.filteredList(searchWord: String): ArrayList<Question> {
    return ArrayList(filter { it.question.contains(searchWord) || it.answer.contains(searchWord) || it.answers.any { it.contains(searchWord) } })
}