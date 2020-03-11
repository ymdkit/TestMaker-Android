package jp.gr.java_conf.foobar.testmaker.service.domain

import io.realm.RealmList

data class Test(
        val id: Long,
        val color: Int,
        val limit: Int,
        val startPosition: Int,
        val title: String,
        val category: String,
        val history: Long,
        val questions: RealmList<Quest>,
        val documentId: String,
        val order: Int
)