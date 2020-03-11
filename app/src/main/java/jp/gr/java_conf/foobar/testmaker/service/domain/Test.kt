package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Test(
        val id: Long = -1,
        val color: Int,
        val limit: Int = 100,
        val startPosition: Int = 0,
        val title: String,
        val category: String = "",
        val history: Long = 0,
        val questions: List<Question> = emptyList(),
        val documentId: String = "",
        val order: Int = 0
) : Parcelable {

    val questionsCorrectCount
        get() = questions.count { it.isCorrect }

    companion object {
        fun createFromRealmTest(realmTest: RealmTest) = Test(
                realmTest.id,
                realmTest.color,
                realmTest.limit,
                realmTest.startPosition,
                realmTest.title ?: "",
                realmTest.getCategory(),
                realmTest.history,
                realmTest.questionsNonNull().map { Question.createFromRealmQuestion(it) },
                realmTest.documentId,
                realmTest.order
        )
    }

}