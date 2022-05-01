package jp.gr.java_conf.foobar.testmaker.service.view.share

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import javax.inject.Inject

@HiltViewModel
class ShowTestsViewModel @Inject constructor(
    private val auth: Auth,
) : ViewModel() {

    fun getUser(): FirebaseUser? = auth.getUser()

}