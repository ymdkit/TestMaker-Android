package jp.gr.java_conf.foobar.testmaker.service.infra.auth

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Auth @Inject constructor() {

    fun getUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun getAuthUIIntent(): Intent {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                        "https://ankimaker.com/terms",
                        "https://ankimaker.com/privacy")
                .build()
    }
}