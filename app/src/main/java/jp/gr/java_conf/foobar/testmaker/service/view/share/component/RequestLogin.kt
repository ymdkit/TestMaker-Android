package jp.gr.java_conf.foobar.testmaker.service.view.share.component

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import jp.gr.java_conf.foobar.testmaker.service.R

@Composable
fun RequestLogin(
    modifier: Modifier = Modifier,
    onSuccess: (FirebaseUser) -> Unit,
    onFailure: (Int?) -> Unit
){

    val launcher = rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) { result ->
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            user?.let{
                onSuccess(it)
            }
        } else {
            onFailure(response?.error?.errorCode)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(id = R.string.msg_error_not_authorized))
        Button(
            modifier = Modifier.height(48.dp),
            onClick = {

            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
            )

            val intent =  AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTosAndPrivacyPolicyUrls(
                    "https://ankimaker.com/terms",
                    "https://ankimaker.com/privacy"
                )
                .build()

            launcher.launch(intent)
        }) {
            Text(stringResource(id = R.string.button_login))
        }
    }
}