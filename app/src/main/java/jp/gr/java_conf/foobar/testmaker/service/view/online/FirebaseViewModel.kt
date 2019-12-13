package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.content.Intent
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTestResult
import jp.gr.java_conf.foobar.testmaker.service.infra.test.TestMakerRepository

class FirebaseViewModel(private val repository: TestMakerRepository, private val auth: Auth) : ViewModel() {

    fun getLocalTests() = repository.getTests()

    fun getTestsQuery() = repository.getTestsQuery()

    suspend fun downloadTest(testId: String): FirebaseTestResult = repository.downloadTest(testId)

    fun convert(test: FirebaseTest) = repository.createObjectFromFirebase(test)

    fun createUser(user: FirebaseUser?) = repository.setUser(user)

    suspend fun uploadTest(test: Test, overview: String) = repository.createTest(test, overview)

    fun getAuthUIIntent(): Intent = auth.getAuthUIIntent()

}