package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.firebase.ui.auth.AuthUI

class SignInRequestContract : ActivityResultContract<Unit, Unit?>() {

    override fun createIntent(context: Context, input: Unit): Intent {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTosAndPrivacyPolicyUrls(
                "https://ankimaker.com/terms",
                "https://ankimaker.com/privacy"
            )
            .build()
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Unit? {
        if (resultCode != Activity.RESULT_OK) return null
        return Unit
    }
}