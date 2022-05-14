package jp.gr.java_conf.foobar.testmaker.service.infra.repository

import com.example.infra.local.db.WorkbookDataSource
import com.example.infra.remote.entity.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.RemoteDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TestMakerRepository @Inject constructor(
    private val remote: RemoteDataSource,
    private val workbookDataSource: WorkbookDataSource,
) {

    suspend fun downloadTest(testId: String): FirebaseTest = withContext(Dispatchers.Default) {
        remote.downloadTest(testId)
    }

    fun createObjectFromFirebase(firebaseTest: FirebaseTest, source: String): Test {

        val test = firebaseTest.toTest()

        test.id = workbookDataSource.generateWorkbookId()
        test.order = test.id.toInt()
        test.source = source

        val questionId = workbookDataSource.generateQuestionId()

        firebaseTest.questions.forEachIndexed { index, it ->
            val question = it.toQuest()
            question.order = index
            question.id = questionId + index
            test.addQuestion(question)
        }

        workbookDataSource.createWorkbook(test)

        return Test.createFromRealmTest(test)
    }

    suspend fun createTestInGroup(test: Test, overview: String, groupId: String) {
        remote.createTest(test = test, overview = overview, isPublic = false, groupId = groupId)
    }
}