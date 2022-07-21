package com.example.ui.core

import android.app.Activity.RESULT_OK
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun RequireAuthentication(
    modifier: Modifier = Modifier,
    isLogin: Boolean,
    onLogin: () -> Unit,
    message: String,
    content: @Composable () -> Unit,
) {

    val context = LocalContext.current
    val launcher =
        rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) {
            if (it.resultCode == RESULT_OK) {
                onLogin()
            } else {
                val response = it.idpResponse
                context.showToast(
                    context.getString(
                        R.string.msg_failure_login,
                        response?.error?.errorCode
                    )
                )
            }
        }

    if (isLogin) {
        content()
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val intent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(
                        arrayListOf(
                            AuthUI.IdpConfig.EmailBuilder().build(),
                            AuthUI.IdpConfig.GoogleBuilder().build()
                        )
                    )
                    .setTosAndPrivacyPolicyUrls(
                        "https://ankimaker.com/terms",
                        "https://ankimaker.com/privacy"
                    )
                    .build()

                launcher.launch(intent)
            }) {
                Text(text = stringResource(id = R.string.button_login))
            }
        }
    }
}