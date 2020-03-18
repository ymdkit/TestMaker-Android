package jp.gr.java_conf.foobar.testmaker.service.view.play

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityPlayBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Question
import jp.gr.java_conf.foobar.testmaker.service.domain.QuestionsBuilder
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.debounceClick
import jp.gr.java_conf.foobar.testmaker.service.extensions.observeNonNull
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import jp.gr.java_conf.foobar.testmaker.service.view.main.TestViewModel
import jp.gr.java_conf.foobar.testmaker.service.view.result.ResultActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Created by keita on 2016/07/17.
 */
class PlayActivity : BaseActivity() {

    internal var number: Int = -1

    private lateinit var soundMistake: SePlayer
    private lateinit var soundRight: SePlayer

    internal lateinit var questions: List<Question>

    private lateinit var inputMethodManager: InputMethodManager

    private var startTime: Long = 0

    private val playViewModel: PlayViewModel by viewModel()
    private val testViewModel: TestViewModel by viewModel()

    private lateinit var test: Test

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        testViewModel.tests.find { it.id == intent.getLongExtra("id", -1L) }?.let {
            test = it
        }

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        initToolBar()

        val binding = DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)
        createAd(binding.adView)

        soundMistake = SePlayer(applicationContext, R.raw.mistake)
        soundRight = SePlayer(applicationContext, R.raw.correct)

        initViews()

        initQuestions()

        testViewModel.testsLiveData.observeNonNull(this) {
            it.find { test.id == it.id }?.let {
                test = it
            }
        }

        startTime = System.currentTimeMillis()

        loadNext(0)

    }

    override fun onPause() {

        play_write_view.hideKeyboard()

        inputMethodManager.hideSoftInputFromWindow(play_complete_view.firstEditText.windowToken, 0)

        super.onPause()
    }

    private fun initQuestions() {

        questions = QuestionsBuilder(test.questions)
                .retry(intent.hasExtra("isRetry"))
                .startPosition(test.startPosition)
                .mistakeOnly(sharedPreferenceManager.refine)
                .shuffle(sharedPreferenceManager.random)
                .limit(test.limit)
                .build()

        testViewModel.update(test.copy(
                questions = test.questions.map {
                    it.copy(isSolved = false)
                }
        ))

        if (questions.isEmpty()) {
            Toast.makeText(baseContext, getString(R.string.msg_null_questions), Toast.LENGTH_LONG).show()
            return
        }
    }

    fun loadNext(second: Long) {

        lifecycleScope.launch {
            delay(second)

            number += 1

            if (questions.size > number) {

                val question = questions[number]

                showProblem(question)

                when (question.type) {

                    Constants.WRITE ->

                        showLayoutWrite()
                    Constants.COMPLETE ->

                        showLayoutComplete(question)

                    Constants.SELECT ->

                        showLayoutSelect(question)

                    Constants.SELECT_COMPLETE ->

                        showLayoutSelectComplete(question)
                }

            } else { //全問終了後

                showResult()

            }
        }
    }

    fun checkAnswer(yourAnswer: String) {
        if (number >= questions.size) return

        play_select_view.invalidate()
        if (questions[number].isCorrect(yourAnswer, sharedPreferenceManager.reverse, sharedPreferenceManager.isCaseInsensitive)) {
            actionCorrect()
        } else {
            actionMistake(yourAnswer)
            play_review_view.setTextAnswer(questions[number].getReversibleAnswer(sharedPreferenceManager.reverse))
        }
    }

    fun checkAnswer(answers: List<String>) { //完答

        if (questions[number].isCorrect(answers, sharedPreferenceManager.isCaseInsensitive)) {

            actionCorrect()

        } else {

            actionMistake(answers.filter { it.isNotEmpty() }.joinToString("\n"))
            play_review_view.setTextAnswer(questions[number].answers.filter { it.isNotEmpty() }.joinToString("\n"))

        }

    }

    private fun actionCorrect() {

        soundRight.playSe()

        testViewModel.update(questions[number].copy(
                isCorrect = true,
                isSolved = true
        ))

        showImageJudge(R.drawable.right)

        if (sharedPreferenceManager.alwaysReview && questions[number].explanation != "") {//正解時も解説を表示

            showLayoutMistake("")

            play_review_view.setTextAnswer(questions[number].answer)

        } else {

            loadNext(600)

        }
    }

    private fun actionMistake(yourAnswer: String) {

        testViewModel.update(questions[number].copy(
                isCorrect = false,
                isSolved = true
        ))
        showLayoutMistake(yourAnswer)

        showImageJudge(R.drawable.mistake)

        soundMistake.playSe()
    }

    private fun showImageJudge(id: Int) {

        image_judge.setImageDrawable(ResourcesCompat.getDrawable(resources, id, null))
        image_judge.visibility = View.VISIBLE
        image_judge.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.alpha_appear))

        Handler().postDelayed({ image_judge.visibility = View.GONE }, 600)

    }

    private fun showLayoutMistake(yourAnswer: String) {

        play_write_view.visibility = View.GONE
        play_complete_view.visibility = View.GONE
        play_select_view.visibility = View.GONE
        play_select_complete_view.visibility = View.GONE

        play_review_view.visibility = View.VISIBLE
        play_review_view.setTextExplanation(questions[number].explanation)
        play_mistake_view.show(yourAnswer)

    }

    private fun showProblem(question: Question) {

        play_problem_view.setTextProblem(question.getReversibleProblem(sharedPreferenceManager.reverse))
        play_problem_view.setTextNumber(getString(R.string.number, (number + 1).toString()))

        play_review_view.visibility = View.GONE
        play_mistake_view.visibility = View.GONE
        play_manual_view.visibility = View.GONE
        play_write_view.visibility = View.GONE
        play_complete_view.visibility = View.GONE
        play_select_view.visibility = View.GONE
        play_select_complete_view.visibility = View.GONE

        showImageProblem(question)

    }

    private fun showImageProblem(question: Question) {

        if (question.imagePath == "") {

            play_problem_view.hideImage()

        } else {

            play_problem_view.showImage()

            if (question.imagePath.contains("/")) {

                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(question.imagePath)

                play_problem_view.setImage(storageRef)

            } else {

                lifecycleScope.launch {
                    play_problem_view.getImageProblem()?.setImageWithGlide(baseContext, playViewModel.loadImage(question.imagePath))
                }
            }
        }
    }

    private fun showResult() {
        ResultActivity.startActivity(this@PlayActivity, test.id, System.currentTimeMillis() - startTime)
    }

    private fun showLayoutWrite() {

        if (sharedPreferenceManager.manual) {

            button_confirm.visibility = View.VISIBLE

            return

        }

        play_write_view.show()

    }

    private fun showLayoutSelect(question: Question) {

        play_select_view.show(question)
        play_select_view.setTextChoices(question, makeChoice(question.others.size))

    }

    private fun showLayoutComplete(question: Question) {

        if (sharedPreferenceManager.manual) {

            button_confirm.visibility = View.VISIBLE

            return

        }

        if (sharedPreferenceManager.reverse) {

            play_write_view.show()

        } else {

            play_complete_view.visibility = View.VISIBLE

            play_complete_view.initEditAnswers(question)

        }

    }

    private fun showLayoutSelectComplete(question: Question) {

        play_select_complete_view.show(question)
        play_select_complete_view.setTextChoices(question, makeChoice(question.others.size))

    }

    private fun makeChoice(num: Int): ArrayList<String> {
        return test.getChoices(num, questions[number].answer, baseContext)
    }

    private fun initViews() {

        play_write_view.setOnClickListener(object : PlayWriteView.OnClickListener {
            override fun onClick(answer: String) {
                checkAnswer(answer)
            }
        })

        play_select_view.setOnClickListener(object : PlaySelectView.OnClickListener {
            override fun onClick(answer: String) {
                checkAnswer(answer)
            }
        })

        play_complete_view.setOnClickListener(object : PlayCompleteView.OnClickListener {
            override fun onClick() {
                checkAnswer(play_complete_view.getAnswers())
            }
        })

        play_select_complete_view.setOnClickListener(object : PlaySelectCompleteView.OnClickListener {
            override fun onClick() {
                checkAnswer(play_select_complete_view.getAnswers())
            }
        })

        play_mistake_view.setOnClickListener(object : PlayMistakeView.OnClickListener {

            override fun onClick() {
                loadNext(0)
            }

        })

        play_manual_view.setOnClickListener(object : PlayManualView.OnClickListener {
            override fun onClickRight() {

                testViewModel.update(questions[number].copy(
                        isCorrect = true,
                        isSolved = true
                ))
                loadNext(60)
            }

            override fun onClickMistake() {

                testViewModel.update(questions[number].copy(
                        isCorrect = false,
                        isSolved = true
                ))
                loadNext(60)

            }
        })

        button_confirm.debounceClick {
            showLayoutManual()
        }
    }

    private fun showLayoutManual() {

        play_write_view.visibility = View.GONE
        play_select_view.visibility = View.GONE
        play_complete_view.visibility = View.GONE
        play_review_view.visibility = View.VISIBLE
        play_manual_view.visibility = View.VISIBLE
        button_confirm.visibility = View.GONE

        val question = questions[number]

        play_review_view.setTextAnswer(question.getReversibleAnswer(sharedPreferenceManager.reverse))

        play_review_view.setTextExplanation(question.explanation)

    }

    companion object {

        fun startActivity(activity: Activity, id: Long) {
            val intent = Intent(activity, PlayActivity::class.java).apply {
                putExtra("id", id)
            }
            activity.startActivity(intent)
        }

        fun startActivity(activity: Activity, id: Long, isRetry: Boolean) {
            val intent = Intent(activity, PlayActivity::class.java).apply {
                putExtra("id", id)
                putExtra("isRetry", isRetry)
            }
            activity.startActivity(intent)
        }
    }
}
