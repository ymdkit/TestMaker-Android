package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.infra.local.db.TestDataSource
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.Test

class TestRepository(private val dataSource: TestDataSource) {

    private var testsLiveData: MutableLiveData<List<Test>> =
        MutableLiveData(dataSource.getAll().map { Test.createFromRealmTest(it) }
        )

    private var tests: List<Test>? = null

    fun getAsLiveData(): LiveData<List<Test>> = testsLiveData

    fun get(): List<Test> = tests ?: dataSource.getAll().map { Test.createFromRealmTest(it) }.also {
        tests = it
    }

    fun get(id: Long): Test = Test.createFromRealmTest(dataSource.get(id))

    fun refresh() {
        tests = dataSource.getAll().map { Test.createFromRealmTest(it) }
        testsLiveData.value = tests
    }

    fun create(test: Test): Long {
        val questionId = dataSource.generateQuestionId()
        val id = dataSource.create(
            test.copy(
                questions = test.questions.mapIndexed { index, question ->
                    question.copy(
                        id = questionId + index,
                        order = index
                    )
                }).toRealmTest()
        )
        refresh()
        return id
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
            dataSource.update(result.toRealmTest())
        } else {
            dataSource.update(test.toRealmTest())
        }
        refresh()
    }

    fun delete(test: Test) {
        dataSource.delete(test.toRealmTest())
        refresh()
    }

    fun swap(from: Test, to: Test) {
        val tmp = from.order
        dataSource.update(from.copy(order = to.order).toRealmTest())
        dataSource.update(to.copy(order = tmp).toRealmTest())
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
        dataSource.update(question.toRealmQuestion())
        refresh()
    }

    fun delete(question: Question) {
        dataSource.delete(question.toRealmQuestion())
        refresh()
    }

    fun swap(from: Question, to: Question) {
        val tmp = from.order
        dataSource.update(from.copy(order = to.order).toRealmQuestion())
        dataSource.update(to.copy(order = tmp).toRealmQuestion())
        refresh()
    }

    fun insertAt(test: Test, question: Question, index: Int) {
        test.questions
            .filter {
                it.order > index
            }.forEach {
                dataSource.update(it.copy(order = it.order + 1).toRealmQuestion())
            }

        create(
            test,
            question.copy(order = index + 1)
        )
        refresh()
    }

}