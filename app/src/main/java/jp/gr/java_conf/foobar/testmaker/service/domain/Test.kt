package jp.gr.java_conf.foobar.testmaker.service.domain

import android.content.Context
import android.os.Parcelable
import com.example.infra.local.entity.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.TestMakerApplication
import jp.gr.java_conf.foobar.testmaker.service.infra.api.TestResponse
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
    val lang: String = "ja",
    val source: String
) : Parcelable {

    fun getRandomExtractedAnswers(exclude: List<String>) =
        questions
            .asSequence()
            .take(100)
            .map {
                when (it.type) {
                    Constants.WRITE, Constants.SELECT -> {
                        listOf(it.answer)
                    }
                    Constants.COMPLETE, Constants.SELECT_COMPLETE -> {
                        it.answers
                    }
                    else -> emptyList()
                }
            }
            .flatten()
            .filter { !exclude.contains(it) }
            .distinct()
            .toList()
            .shuffled()

    val questionsCorrectCount
        get() = questions.count { it.isCorrect }

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

    fun getColorId(context: Context): Int =
        context.resources.getIntArray(R.array.color_list).indexOf(color).coerceAtLeast(0)
    
    fun toRealmTest(): RealmTest {
        val realmTest = RealmTest()
        realmTest.id = id
        realmTest.color = color
        realmTest.limit = limit
        realmTest.startPosition = startPosition
        realmTest.title = title
        realmTest.setCategory(category)
        realmTest.history = history
        questions.forEach { realmTest.addQuestion(it.toRealmQuestion()) }
        realmTest.documentId = documentId
        realmTest.order = order
        realmTest.source = source
        return realmTest
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
            realmTest.order,
            source = realmTest.source
        )

        fun createFromTestResponse(testResponse: TestResponse) = Test(
            title = testResponse.title,
            lang = testResponse.lang,
            questions = testResponse.questions.mapIndexed { index, it ->
                Question
                    .createFromQuestionResponse(it, order = index)
            },
            source = CreateTestSource.FILE_IMPORT.title
        )
    }

}