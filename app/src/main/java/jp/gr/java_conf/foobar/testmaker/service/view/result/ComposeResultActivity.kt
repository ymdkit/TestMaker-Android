package jp.gr.java_conf.foobar.testmaker.service.view.result

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.play.core.review.ReviewManagerFactory
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.extensions.showToast
import jp.gr.java_conf.foobar.testmaker.service.infra.auth.Auth
import jp.gr.java_conf.foobar.testmaker.service.view.main.MainActivity
import jp.gr.java_conf.foobar.testmaker.service.view.play.PlayActivity
import jp.gr.java_conf.foobar.testmaker.service.view.result.ui.theme.TestMakerAndroidTheme
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.DialogMenuItem
import jp.gr.java_conf.foobar.testmaker.service.view.share.ListDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class ComposeResultActivity : BaseActivity() {

    companion object {
        const val ARGUMENT_TEST_ID = "id"
        const val ARGUMENT_DURATION = "duration"

        const val COUNT_REQUEST_REVIEW = 5

        fun startActivity(activity: Activity, id: Long, duration: Long) {
            val intent = Intent(activity, ComposeResultActivity::class.java).apply {
                putExtra(ARGUMENT_TEST_ID, id)
                putExtra(ARGUMENT_DURATION, duration)
            }
            activity.startActivity(intent)
        }
    }

    private val testId: Long by lazy { intent.getLongExtra(ARGUMENT_TEST_ID, -1L) }

    private val viewModel: ResultViewModel by viewModel { parametersOf(testId) }
    private val auth: Auth by inject()

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestMakerAndroidTheme {
                Scaffold(
                    topBar = {
                        MyTopAppBar(getString(R.string.label_result))
                    },
                    content = {
                        Surface(color = MaterialTheme.colors.surface) {
                            Column {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .verticalScroll(state = ScrollState(0))
                                        .weight(weight = 1f, fill = true)
                                ) {
                                    ItemPieChart(
                                        dataSet =
                                        PieDataSet(
                                            viewModel.scoreList.map {
                                                PieEntry(it)
                                            }, ""
                                        ),
                                        centerText = viewModel.scoreText
                                    )

                                    WideOutlinedButton(
                                        onCLick = {
                                            MainActivity.startActivityWithClear(this@ComposeResultActivity)
                                        },
                                        text = getString(R.string.home),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                    WideOutlinedButton(
                                        onCLick = { onCLickRetry() },
                                        text = getString(R.string.retry),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )

                                    AnimatedVisibility(
                                        visible = sharedPreferenceManager.uploadStudyPlus == resources.getStringArray(
                                            R.array.upload_setting_study_plus_values
                                        )[2]
                                    ) {
                                        WideOutlinedButton(
                                            onCLick = {
                                                viewModel.createStudyPlusRecord(
                                                    intent.getLongExtra(
                                                        ARGUMENT_DURATION,
                                                        0
                                                    ), this@ComposeResultActivity
                                                )
                                            },
                                            text = getString(R.string.menu_upload_studyplus),
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    }

                                    Text(
                                        text = getString(R.string.label_result_questions),
                                        color = MaterialTheme.colors.primary,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    viewModel.questions.mapIndexed { index, it ->
                                        ItemResultModel(
                                            index + 1,
                                            it.question,
                                            it.answer,
                                            it.isCorrect
                                        ).ItemResult()
                                    }
                                }

                                ComposeAdView(isRemovedAd = sharedPreferenceManager.isRemovedAd)
                            }
                        }
                    }
                )
            }
        }

        requestReview()

        auth.getUser()?.let { user ->
            viewModel.createAnswerHistory(user)
        }

        when (sharedPreferenceManager.uploadStudyPlus) {
            resources.getStringArray(R.array.upload_setting_study_plus_values)[1] ->
                viewModel.createStudyPlusRecord(intent.getLongExtra("duration", 0), this)
        }
    }

    override fun onBackPressed() {
        MainActivity.startActivityWithClear(this@ComposeResultActivity)
        super.onBackPressed()
    }

    private fun requestReview() {

        sharedPreferenceManager.playCount += 1
        if (sharedPreferenceManager.playCount != COUNT_REQUEST_REVIEW) return

        val manager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener {
            if (it.isSuccessful) {
                val reviewInfo = it.result
                manager.launchReviewFlow(this@ComposeResultActivity, reviewInfo)
            }
        }
    }

    private fun onCLickRetry() {
        ListDialogFragment(
            getString(R.string.retry),
            listOf(
                DialogMenuItem(
                    title = getString(R.string.result_dialog_item_retry_all),
                    iconRes = R.drawable.ic_play_arrow_white_24dp,
                    action = { retryAllQuestions() }),
                DialogMenuItem(
                    title = getString(R.string.result_dialog_item_retry_only_incorrect),
                    iconRes = R.drawable.ic_baseline_error_24,
                    action = { retryOnlyInCorrectQuestions() })
            )
        ).show(supportFragmentManager, "TAG")
    }

    private fun retryAllQuestions() {
        sharedPreferenceManager.refine = false
        PlayActivity.startActivity(this, testId, true)
    }

    private fun retryOnlyInCorrectQuestions() {
        if (viewModel.questions.any { !it.isCorrect }) {
            sharedPreferenceManager.refine = true
            PlayActivity.startActivity(this, testId, true)
        } else {
            showToast(getString(R.string.message_null_wrongs))
        }
    }
}

@Composable
fun MyTopAppBar(title: String) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = MaterialTheme.colors.onPrimary
            )
        },
        backgroundColor = MaterialTheme.colors.primary
    )
}

@Composable
fun WideOutlinedButton(onCLick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onCLick,
        modifier = modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp)

    ) {
        Text(text)
    }
}

@Composable
fun ItemPieChart(dataSet: PieDataSet, centerText: String) {
    AndroidView(
        factory = {
            PieChart(it).apply {
                this.data = PieData(dataSet.apply {
                    setDrawValues(false)
                    colors = listOf(R.color.colorAccent, R.color.colorPrimary).map { id ->
                        ContextCompat.getColor(context, id)
                    }
                })
                this.centerText = centerText
                setCenterTextSize(24f)
                setCenterTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimary
                    )
                )
                legend.isEnabled = false
                setDrawEntryLabels(false)
                animateXY(500, 500)
                description.isEnabled = false
            }
        }, modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    )
}

@Composable
fun ComposeAdView(isRemovedAd: Boolean) {
    if(isRemovedAd) return

    AndroidView(
        factory = {
            AdView(it).apply {
                adSize = AdSize.BANNER
                adUnitId = "ca-app-pub-8942090726462263/8420884238"
                loadAd(AdRequest.Builder().build())
            }
        },
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
    )
}