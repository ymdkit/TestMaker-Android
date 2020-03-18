package jp.gr.java_conf.foobar.testmaker.service

import io.realm.Sort

object Constants {

    const val WRITE = 0
    const val SELECT = 1
    const val COMPLETE = 2
    const val SELECT_COMPLETE = 3

    const val NOT_EDITING = 0
    const val EDIT_QUESTION = 1
    const val EDIT_CONFIG = 2

    const val OTHER_SELECT_MAX = 5
    const val ANSWER_MAX = 4
    const val SELECT_COMPLETE_MAX = 6
}

enum class SortTest(val column: String, val sort: Sort) {
    TITLE_DESCENDING("title", Sort.DESCENDING),
    TITLE_ASCENDING("title", Sort.ASCENDING),
    HISTORY("history", Sort.DESCENDING);
}
