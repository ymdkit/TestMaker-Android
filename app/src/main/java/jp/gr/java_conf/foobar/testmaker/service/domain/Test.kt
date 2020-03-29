package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication
import kotlinx.android.parcel.Parcelize
import java.util.*

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

    fun getChoices(size: Int, answer: String): ArrayList<String> {

        val result = arrayListOf<String>()

        for (q in questions.take(100).shuffled()) {
            if (result.size >= size) break

            when (q.type) {
                Constants.WRITE, Constants.SELECT -> {
                    if (q.answer != answer) result.add(q.answer)
                }
                Constants.COMPLETE, Constants.SELECT_COMPLETE -> {
                    if (q.answers.isNotEmpty()) {
                        if (q.answers[0] != answer) result.add(q.answers[0])
                    }
                }
            }
        }

        while (result.size < size) {
            result.add(TestMakerApplication.instance.applicationContext.getString(R.string.message_not_auto))
        }

        return result
    }

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