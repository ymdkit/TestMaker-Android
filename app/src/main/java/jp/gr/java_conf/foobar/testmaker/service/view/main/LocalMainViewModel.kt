package jp.gr.java_conf.foobar.testmaker.service.view.main

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.domain.RealmTest
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.infra.repository.TestMakerRepository

class LocalMainViewModel(private val repository: TestMakerRepository, private val auth: Auth) : ViewModel() {

    suspend fun uploadTest(test: RealmTest, documentId: String): String = repository.createTest(test, "")

    fun getUser(): FirebaseUser? = auth.getUser()

}