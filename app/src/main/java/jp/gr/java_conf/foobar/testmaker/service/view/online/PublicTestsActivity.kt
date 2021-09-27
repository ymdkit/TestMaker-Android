package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ExperimentalGraphicsApi
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.online.PublicTestsActivity.Companion.COLOR_MAX
import jp.gr.java_conf.foobar.testmaker.service.view.result.MyTopAppBar
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PublicTestsActivity : ComponentActivity() {

    companion object {
        const val COLOR_MAX = 8F

        fun startActivity(activity: Activity) =
            activity.startActivity(
                Intent(
                    activity,
                    PublicTestsActivity::class.java,
                )
            )
    }

    private val viewModel: FirebaseViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()
    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val logger: TestMakerLogger by inject()


    @ExperimentalGraphicsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val tests by viewModel.tests.observeAsState()

            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        MyTopAppBar(getString(R.string.label_public_tests))
                    },
                    content = {
                        Surface(color = MaterialTheme.colors.surface) {
                            Column {
                                Column(
                                    modifier = Modifier
                                        .verticalScroll(state = ScrollState(0))
                                        .weight(weight = 1f, fill = true)
                                        .padding(top = 16.dp)
                                ) {

                                    tests?.map {
                                        ItemPublicTest(it)
                                    }
                                }

                                Button(
                                    onClick = {
                                        logger.logEvent("upload_from_firebase_activity")
                                        UploadTestActivity.startActivity(this@PublicTestsActivity)
                                    },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        backgroundColor = MaterialTheme.colors.secondary
                                    ),

                                ) {
                                    Text(text = getString(R.string.button_upload_test), color = MaterialTheme.colors.onSecondary)
                                }

                                ComposeAdView(isRemovedAd = sharedPreferenceManager.isRemovedAd)
                            }
                        }
                    }
                )
            }
        }
        viewModel.getTests()
    }
}

@ExperimentalGraphicsApi
@Composable
fun ItemPublicTest(test: FirebaseTest) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_baseline_description_24),
            contentDescription = "icon test",
            colorFilter = ColorFilter.tint(
                Color.Companion.hsv(360F * test.color.toFloat() / COLOR_MAX, 0.5F, 0.9F),
                BlendMode.SrcIn
            ),
            modifier = Modifier
                .width(24.dp)
                .height(24.dp)
        )

        Text(
            text = test.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}