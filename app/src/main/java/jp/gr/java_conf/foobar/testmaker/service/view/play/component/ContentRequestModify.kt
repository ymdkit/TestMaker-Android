package jp.gr.java_conf.foobar.testmaker.service.view.play.component

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.startActivity
import jp.gr.java_conf.foobar.testmaker.service.BuildConfig
import jp.gr.java_conf.foobar.testmaker.service.R

@Composable
fun ContentRequestModify(questionId: Int){
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth()){
        OutlinedButton(modifier = Modifier.align(Alignment.End), onClick = {
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf("testmaker.contact@gmail.com")
            )
            emailIntent.putExtra(
                Intent.EXTRA_SUBJECT,
                context.getString(R.string.email_subject_request_modify,
                    questionId.toString())
            )
            emailIntent.putExtra(
                Intent.EXTRA_TEXT,
                context.getString(
                    R.string.email_body_requet_modify,
                    BuildConfig.VERSION_NAME
                )
            )
            startActivity(context, Intent.createChooser(emailIntent, null), null)
        }) {
            Text(text = stringResource(id = R.string.button_request_modify))
        }
    }
}