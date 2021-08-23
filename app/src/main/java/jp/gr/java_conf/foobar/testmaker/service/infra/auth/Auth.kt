package jp.gr.java_conf.foobar.testmaker.service.infra.auth

import android.content.Intent
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

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
                        "https://ankimaker.com/terms",
                        "https://ankimaker.com/privacy")
                .build()
    }

    fun updateProfile(userName: String, completion: () -> Unit) {
        val user = getUser() ?: return

        val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(userName).build()

        user.updateProfile(profileUpdates).addOnSuccessListener {
            completion()
        }
    }

}