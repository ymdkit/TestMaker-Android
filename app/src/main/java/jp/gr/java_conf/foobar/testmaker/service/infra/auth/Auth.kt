package jp.gr.java_conf.foobar.testmaker.service.infra.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Auth @Inject constructor() {

    fun getUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser
}