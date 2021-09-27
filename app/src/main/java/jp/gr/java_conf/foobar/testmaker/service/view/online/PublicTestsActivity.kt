package jp.gr.java_conf.foobar.testmaker.service.view.online

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.domain.CreateTestSource
import jp.gr.java_conf.foobar.testmaker.service.extensions.executeJobWithDialog
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.showErrorToast
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.db.SharedPreferenceManager
import jp.gr.java_conf.foobar.testmaker.service.infra.firebase.FirebaseTest
import jp.gr.java_conf.foobar.testmaker.service.infra.logger.TestMakerLogger
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.online.PublicTestsActivity.Companion.COLOR_MAX
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import jp.gr.java_conf.foobar.testmaker.service.view.share.component.ComposeAdView
import jp.gr.java_conf.foobar.testmaker.service.view.ui.theme.TestMakerAndroidTheme
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class PublicTestsActivity : AppCompatActivity() {

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
    private val sharedPreferenceManager: SharedPreferenceManager by inject()
    private val logger: TestMakerLogger by inject()


    @ExperimentalGraphicsApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val tests by viewModel.tests.observeAsState(emptyList())
            val isRefreshing by viewModel.loading.observeAsState(true)
            val isSearching = mutableStateOf(false)

            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                if (isSearching.value) {
                                    SearchTextField(
                                        modifier = Modifier.fillMaxWidth()
                                    ){
                                        viewModel.getTests(it)
                                    }
                                } else {
                                    Text(
                                        text = getString(R.string.label_public_tests),
                                        color = MaterialTheme.colors.onPrimary
                                    )
                                }

                            },
                            backgroundColor = MaterialTheme.colors.primary,
                            actions = {
                                IconButton(onClick = {
                                    isSearching.value = !isSearching.value
                                }) {
                                    Image(
                                        painter = painterResource(
                                            id =
                                            if (isSearching.value) R.drawable.ic_close_white
                                            else R.drawable.ic_baseline_search_24
                                        ),
                                        contentDescription = "search",
                                    )
                                }
                            },
                        )
                    },
                    content = {
                        Surface(color = MaterialTheme.colors.surface) {
                            Column {
                                SwipeRefresh(
                                    modifier = Modifier
                                        .weight(weight = 1f, fill = true),
                                    state = rememberSwipeRefreshState(isRefreshing),
                                    onRefresh = {
                                        viewModel.getTests()
                                    }) {

                                    Column(
                                        modifier = Modifier.verticalScroll(state = rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        tests.map {
                                            ItemPublicTest(it, onClick = { test ->
                                                onClickTest(test)
                                            })
                                        }
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
                                    Text(
                                        text = getString(R.string.button_upload_test),
                                        color = MaterialTheme.colors.onSecondary
                                    )
                                }

                                ComposeAdView(isRemovedAd = sharedPreferenceManager.isRemovedAd)
                            }
                        }
                    }
                )
            }
        }
        viewModel.getTests()

        viewModel.error.observeNonNull(this) {
            showErrorToast(it)
        }
    }

    private fun onClickTest(test: FirebaseTest){
        ListDialogFragment.newInstance(
            test.name,
            listOf(
                DialogMenuItem(
                    title = getString(R.string.download),
                    iconRes = R.drawable.ic_file_download_white,
                    action = { downloadTest(test) }),
                DialogMenuItem(
                    title = getString(R.string.info),
                    iconRes = R.drawable.ic_info_white,
                    action = { showInfoTest(test) }),
                DialogMenuItem(
                    title = getString(R.string.report),
                    iconRes = R.drawable.ic_baseline_flag_24,
                    action = { reportTest(test) })
            )
        ).show(
            this@PublicTestsActivity.supportFragmentManager,
            "TAG"
        )
    }

    private fun downloadTest(test: FirebaseTest) {

        executeJobWithDialog(
            title = getString(R.string.downloading),
            task = {
                viewModel.downloadTest(test.documentId)
            },
            onSuccess = {
                viewModel.convert(it)

                Toast.makeText(
                    this,
                    getString(R.string.msg_success_download_test, it.name),
                    Toast.LENGTH_SHORT
                ).show()
                logger.logCreateTestEvent(it.name, CreateTestSource.PUBLIC_DOWNLOAD.title)
                val intent = Intent(this, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                startActivity(intent)
            },
            onFailure = {
                showToast(getString(R.string.msg_failure_download_test))
            }
        )
    }

    private fun showInfoTest(test: FirebaseTest) {

        ListDialogFragment.newInstance(
            test.name,
            listOf(
                DialogMenuItem(
                    title = getString(R.string.text_info_creator, test.userName),
                    iconRes = R.drawable.ic_account,
                    action = { }),
                DialogMenuItem(
                    title = getString(R.string.text_info_created_at, test.getDate()),
                    iconRes = R.drawable.ic_baseline_calendar_today_24,
                    action = { }),
                DialogMenuItem(
                    title = getString(R.string.text_info_overview, test.overview),
                    iconRes = R.drawable.ic_baseline_description_24,
                    action = { })
            )
        ).show(supportFragmentManager, "TAG")

    }

    private fun reportTest(test: FirebaseTest) {

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("testmaker.contact@gmail.com"))
        emailIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.report_subject, test.documentId)
        )
        emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.report_body))
        startActivity(Intent.createChooser(emailIntent, null))

    }
}

@Composable
fun SearchTextField(modifier:Modifier = Modifier, onSearch:(String) -> Unit) {

    val searchWord = remember {
        mutableStateOf(TextFieldValue())
    }
    val focusRequester by remember { mutableStateOf(FocusRequester())}

    BasicTextField(
        value = searchWord.value,
        onValueChange = {
            searchWord.value = it
        },
        keyboardActions = KeyboardActions(onSearch = {
            onSearch(searchWord.value.text)
        }),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        singleLine = true,
        cursorBrush = SolidColor(MaterialTheme.colors.onPrimary),
        modifier = modifier.focusRequester(focusRequester),
        textStyle = TextStyle(color = MaterialTheme.colors.onPrimary)
    )

    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }

}

@ExperimentalGraphicsApi
@Composable
fun ItemPublicTest(test: FirebaseTest, onClick: (FirebaseTest) -> Unit) {
    Row(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clickable(onClick = { onClick(test) }),

        ) {
        Row(
            modifier = Modifier.padding(16.dp)
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
                fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp),
                maxLines = 1
            )
        }
    }
}