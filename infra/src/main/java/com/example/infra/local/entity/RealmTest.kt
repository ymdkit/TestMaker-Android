package com.example.infra.local.entity

import com.example.core.TestMakerColor
import com.example.domain.model.Workbook
import com.example.domain.model.WorkbookId
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by keita on 2017/02/08.
 */

open class RealmTest : RealmObject() {

    companion object {
        fun fromWorkbook(workbook: Workbook): RealmTest =
            RealmTest().apply {
                id = workbook.id.value
                themeColor = workbook.color.name
                title = workbook.name
                order = workbook.order
                documentId = workbook.remoteId
                setCategory(workbook.folderName)
                workbook.questionList.forEach { addQuestion(RealmQuestion.fromQuestion(it)) }
            }
    }

    @PrimaryKey
    var id: Long = 0

    @Deprecated("migrate to themeColor")
    var color: Int = 0
    var themeColor: String = TestMakerColor.BLUE.name
    var title: String? = null
    private var category: String? = null
    private var questions: RealmList<RealmQuestion>? = null
    var documentId: String = ""
    var order: Int = 0
    var source: String = "undefined"

    fun setCategory(c: String) {
        category = c
    }

    fun getCategory(): String {
        return category ?: ""
    }

    fun addQuestion(q: RealmQuestion) {

        questions ?: run { questions = RealmList() }

        questions?.add(q)
    }

    fun toWorkbook(folderNameList: List<String>): Workbook = Workbook(
        id = WorkbookId(value = id),
        remoteId = documentId,
        name = title ?: "no title",
        color = TestMakerColor.values().firstOrNull { it.name == themeColor }
            ?: TestMakerColor.BLUE,
        order = order,
        folderName = if (folderNameList.contains(getCategory())) getCategory() else "",
        questionList = questions?.map {
            it.toQuestion()
        } ?: listOf()
    )

    fun getQuestions() = questions ?: listOf()
}
