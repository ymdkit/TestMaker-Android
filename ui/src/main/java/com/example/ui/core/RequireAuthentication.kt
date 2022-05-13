package com.example.ui.core

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.ui.R
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract

@Composable
fun RequireAuthentication(
    isLogin: Boolean,
    content: @Composable () -> Unit,
    onLogin: () -> Unit
) {

    val launcher =
        rememberLauncherForActivityResult(FirebaseAuthUIActivityResultContract()) {
            onLogin()
        }

    if (isLogin) {
        content()
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.msg_not_login_in_group)
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