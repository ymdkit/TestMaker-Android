package jp.gr.java_conf.foobar.testmaker.service.view.edit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionFormat
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import kotlinx.coroutines.launch

class CreateQuestionActivity : AppCompatActivity() {

    companion object {
        const val ARGUMENT_TEST_ID = "testId"

        fun startActivity(activity: Activity, testId: Long) {
            val intent = Intent(activity, CreateQuestionActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, testId)
            }
            activity.startActivity(intent)
        }
    }

    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = getString(R.string.title_activity_edit_question),
                                    color = MaterialTheme.colors.onPrimary
                                )
                            },
                            elevation = 0.dp,
                            backgroundColor = MaterialTheme.colors.primary
                        )
                    },
                    content = {
                        Column {

                            val pagerState = rememberPagerState(
                                pageCount = QuestionFormat.values().size,
                                initialOffscreenLimit = 4,
                                infiniteLoop = false,
                                initialPage = 0
                            )
                            val tabIndex = pagerState.currentPage
                            val coroutineScope = rememberCoroutineScope()

                            TabRow(selectedTabIndex = tabIndex) {
                                QuestionFormat.values().forEachIndexed { index, format ->
                                    Tab(
                                        selected = tabIndex == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = {
                                            Text(
                                                stringResource(
                                                    id = when (format) {
                                                        QuestionFormat.WRITE -> R.string.write
                                                        QuestionFormat.SELECT -> R.string.select
                                                        QuestionFormat.COMPLETE -> R.string.complete
                                                        QuestionFormat.SELECT_COMPLETE -> R.string.select_complete
                                                    }
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

}