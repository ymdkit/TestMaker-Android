package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import com.example.infra.local.db.WorkbookDataSource
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
}