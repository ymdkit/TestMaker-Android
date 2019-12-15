package jp.gr.java_conf.foobar.testmaker.service.infra.auth

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Auth {

    fun getUser(): FirebaseUser? = FirebaseAuth.getInstance().currentUser

    fun logOut() = FirebaseAuth.getInstance().signOut()

    fun getAuthUIIntent(): Intent {

        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build())

        return AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                        "https://testmaker-1cb29.firebaseapp.com/terms",
                        "https://testmaker-1cb29.firebaseapp.com/privacy")
                .build()
    }

}