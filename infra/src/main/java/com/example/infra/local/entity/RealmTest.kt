package com.example.infra.local.entity

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by keita on 2017/02/08.
 */

open class RealmTest : RealmObject() {

    @PrimaryKey
    var id: Long = 0
    var color: Int = 0
    var limit: Int = 100
    var startPosition: Int = 0
    var title: String? = null
    private var category: String? = null
    var history: Long = 0
    private var questions: RealmList<Quest>? = null
    var documentId: String = ""
    var order: Int = 0
    var source: String = "undefined"

    fun setCategory(c: String) {
        category = c
    }

    fun getCategory(): String {
        return category ?: ""
    }

    fun addQuestion(q: Quest) {

        questions ?: run { questions = RealmList() }

        questions?.add(q)
    }

    fun questionsNonNull(): List<Quest> = questions?.sortedBy { it.order } ?: listOf()

    fun resetAchievement() {

        questions ?: return
        questions!!.forEach { it.correct = false }

    }
}
