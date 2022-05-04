package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import com.example.infra.local.db.WorkbookDataSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestRepository @Inject constructor(
    private val dataSource: WorkbookDataSource
) {

    private var tests: List<Test>? = null

    fun get(): List<Test> =
        tests ?: dataSource.getWorkbookList().map { Test.createFromRealmTest(it) }.also {
            tests = it
        }

    fun get(id: Long): Test = Test.createFromRealmTest(dataSource.getWorkbook(id))

    fun refresh() {
        tests = dataSource.getWorkbookList().map { Test.createFromRealmTest(it) }
    }

    fun create(test: Test) {
        val workbookId = dataSource.generateWorkbookId()
        val questionId = dataSource.generateQuestionId()
        dataSource.createWorkbook(
            test.copy(
                id = workbookId,
                order = workbookId.toInt(),
                questions = test.questions.mapIndexed { index, question ->
                    question.copy(
                        id = questionId + index,
                        order = index
                    )
                }).toRealmTest()
        )
        refresh()
    }

    fun update(test: Test) {
        val questionId = dataSource.generateQuestionId()
        if (test.questions.size >= 2 && test.questions[0].id == test.questions[1].id) {
            val result = test.copy(questions =
            test.questions.mapIndexed { index, question ->
                question.copy(
                    id = questionId + index,
                    order = index
                )
            })
            dataSource.updateQuestion(result.toRealmTest())
        } else {
            dataSource.updateQuestion(test.toRealmTest())
        }
        refresh()
    }

    fun create(test: Test, question: Question): Long {

        val questionId = dataSource.generateQuestionId()
        update(
            test.copy(
                questions = get(test.id).questions + listOf(
                    question.copy(
                        id = questionId,
                        order = questionId.toInt()
                    )
                )
            )
        )

        refresh()
        return questionId
    }

    fun update(question: Question) {
        dataSource.updateQuestion(question.toRealmQuestion())
        refresh()
    }

    fun delete(question: Question) {
        dataSource.deleteQuestion(question.toRealmQuestion())
        refresh()
    }
}