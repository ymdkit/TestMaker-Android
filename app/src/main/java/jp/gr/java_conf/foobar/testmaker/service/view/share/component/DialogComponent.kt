package jp.gr.java_conf.foobar.testmaker.service.view.share.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DangerDialogContent(
    title: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.padding(12.dp)) {
        Text(text = title, color = MaterialTheme.colors.onBackground)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                backgroundColor = MaterialTheme.colors.error
            ),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth()
        ) {
            Text(
                text = buttonText,
            )
        }
    }
}