package jp.gr.java_conf.foobar.testmaker.service.view.share

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class ShowTestsViewModel(private val repository: TestMakerRepository,private val auth: Auth) : ViewModel() {

    fun getTest(testId: Long): Test = repository.getTest(testId)
    fun getTestClone(testId: Long): Test = repository.getTestClone(testId)
    fun deleteTest(test: Test) = repository.deleteTest(test)
    fun updateHistory(test: Test) = repository.updateHistory(test)
    fun updateStart(test: Test, start: Int) = repository.updateStart(test, start)
    fun updateLimit(test: Test, limit: Int) = repository.updateLimit(test, limit)
    suspend fun uploadTest(test: Test, overview: String): String {
        return repository.createTest(test, overview)
    }
    fun getUser(): FirebaseUser? = auth.getUser()
    fun fetchTests() = repository.fetchTests()
    fun fetchCategories() = repository.fetchCategories()


}