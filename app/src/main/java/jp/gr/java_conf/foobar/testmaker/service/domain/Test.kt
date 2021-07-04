package jp.gr.java_conf.foobar.testmaker.service.domain

import android.os.Parcelable
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication
import kotlinx.parcelize.Parcelize

@Parcelize
data class Test(
        val id: Long = -1,
        val color: Int = TestMakerApplication.instance.applicationContext.resources.getIntArray(R.array.color_list)[0],
        val limit: Int = 100,
        val startPosition: Int = 0,
        val title: String,
        val category: String = "",
        val history: Long = 0,
        val questions: List<Question> = emptyList(),
        val documentId: String = "",
        val order: Int = 0,
        val lang: String = "ja"
) : Parcelable {

    val randomExtractedAnswers
        get() = questions.take(100).map {
            when (it.type) {
                Constants.WRITE, Constants.SELECT -> {
                    listOf(it.answer)
                }
                Constants.COMPLETE, Constants.SELECT_COMPLETE -> {
                    it.answers
                }
                else -> emptyList()
            }
        }.flatten().distinct().shuffled()

    val questionsCorrectCount
        get() = questions.count { it.isCorrect }

    fun getChoices(size: Int, answer: String, emptyString: String) =
            List(size) { emptyString }.mapIndexed { index, value ->
                if (index < randomExtractedAnswers.size && randomExtractedAnswers[index] != answer) randomExtractedAnswers[index] else value
            }

    fun getChoices(size: Int, answers: List<String>, emptyString: String) =
            List(size) { emptyString }.mapIndexed { index, value ->
                if (index < randomExtractedAnswers.size && !answers.contains(randomExtractedAnswers[index])) randomExtractedAnswers[index] else value
            }

    val escapedTest: Test
        get() {
            return copy(questions =
            questions.map {
                it.copy(
                        question = it.question.replace("\n", "¥n"),
                        answers = it.answers.map { it.replace("\n", "¥n") },
                        others = it.others.map { it.replace("\n", "¥n") },
                        explanation = it.explanation.replace("\n", "¥n")
                )
            })
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