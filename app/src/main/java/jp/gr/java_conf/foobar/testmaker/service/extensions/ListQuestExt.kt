package jp.gr.java_conf.foobar.testmaker.service.extensions

import jp.gr.java_conf.foobar.testmaker.service.domain.Question

fun List<Question>.filteredList(searchWord: String): List<Question> {
    return filter { it.question.contains(searchWord) || it.answer.contains(searchWord) || it.answers.any { it.contains(searchWord) } }
}