package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Scaffold
import jp.gr.java_conf.foobar.testmaker.service.view.result.MyTopAppBar
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme

class AnswerWorkbookActivity: AppCompatActivity() {

    companion object {

        private const val KEY_WORKBOOK_ID = "workbook_id"
        private const val KEY_IS_RETRY = "is_retry"

        fun startActivity(activity: Activity, workbookId: Long, isRetry: Boolean = false) {
            val intent = Intent(activity, AnswerWorkbookActivity::class.java).apply {
                putExtra(KEY_WORKBOOK_ID, workbookId)
                putExtra(KEY_IS_RETRY, isRetry)
            }
            activity.startActivity(intent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        MyTopAppBar("")
                    },
                    content = {

                    }
                )
            }
        }
    }
}