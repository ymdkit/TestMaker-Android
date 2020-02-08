package jp.gr.java_conf.foobar.testmaker.service.view.play

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
import com.google.firebase.storage.FirebaseStorage
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.databinding.ActivityPlayBinding
import jp.gr.java_conf.foobar.testmaker.service.domain.Quest
import jp.gr.java_conf.foobar.testmaker.service.domain.Test
import jp.gr.java_conf.foobar.testmaker.service.extensions.setImageWithGlide
import jp.gr.java_conf.foobar.testmaker.service.view.result.ResultActivity
import jp.gr.java_conf.foobar.testmaker.service.view.share.BaseActivity
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by keita on 2016/07/17.
 */
class PlayActivity : BaseActivity() {

    internal var number: Int = -1

    private lateinit var soundMistake: SePlayer
    private lateinit var soundRight: SePlayer

    internal lateinit var questions: ArrayList<Quest>

    private lateinit var inputMethodManager: InputMethodManager

    private var startTime: Long = 0

    private var allAnswers: ArrayList<String> = arrayListOf()

    private val playViewModel: PlayViewModel by viewModel()

    private lateinit var test: Test


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        initToolBar()

        playViewModel.testId = intent.getLongExtra("testId", -1)

        val binding = DataBindingUtil.setContentView<ActivityPlayBinding>(this, R.layout.activity_play)
        createAd(binding.adView)

        soundMistake = SePlayer(applicationContext, R.raw.mistake)
        soundRight = SePlayer(applicationContext, R.raw.correct)

        initViews()

        initQuestions()

        startTime = System.currentTimeMillis()

        loadNext(0)

    }

    private fun initQuestions() {

        test = playViewModel.getTest()

        questions = if (intent.hasExtra("redo")) test.getQuestionsSolved()
        else ArrayList(test.questionsNonNull())

        playViewModel.resetSolving()

        if (!intent.hasExtra("redo")) questions = ArrayList(questions.drop(playViewModel.getTest().startPosition))

        if (sharedPreferenceManager.refine) questions = questions.filter { !it.correct } as ArrayList<Quest>

        if (sharedPreferenceManager.random) questions.shuffle()

        if (playViewModel.getTest().limit < questions.size) questions = ArrayList(questions.take(playViewModel.getTest().limit))

        if (questions.size < 1) {
            Toast.makeText(baseContext, getString(R.string.msg_null_questions), Toast.LENGTH_LONG).show()
            return
        }
    }

    override fun onPause() {

        play_write_view.hideKeyboard()

        inputMethodManager.hideSoftInputFromWindow(play_complete_view.firstEditText.windowToken, 0)

        super.onPause()
    }

    fun checkAnswer(yourAnswer: String) {
        if(number >= questions.size) return

        play_select_view.invalidate()

        val answer = questions[number].getAnswer(isReverse(questions[number]))

        if (isEqual(yourAnswer, answer)) {

            actionCorrect()

        } else {

            actionMistake(yourAnswer)

            play_review_view.setTextAnswer(answer)

        }
    }

    private fun isEqual(yourAnswer: String, answer: String): Boolean {

        if (sharedPreferenceManager.isCaseInsensitive) return yourAnswer.toLowerCase() == answer.toLowerCase()

        return yourAnswer == answer
    }

    fun checkAnswer(answers: ArrayList<String?>) { //完答

        var loop = true

        for (answer in answers) {

            loop = false

            for (k in 0 until questions[number].answers.size) if (isEqual(answer
                            ?: "0", questions[number].answers[k]?.selection ?: "1")) loop = true

            if (!loop) break

        }

        if (loop) loop = answers.size == questions[number].answers.size //必要条件だけ答えてもダメ

        if (questions[number].isCheckOrder) {

            for ((index, answer) in answers.withIndex()) {

                if (answer != questions[number].answers[index]?.selection) {
                    loop = false
                    break
                }

            }

        } else {
            if (loop) loop = answers.distinct().size == answers.size //同じ解答を繰り返してもダメ
        }

        if (loop) {

            actionCorrect()

        } else {

            val yourAnswer = StringBuilder()

            for (your in answers) if (your != "") yourAnswer.append(your).append(" ")

            actionMistake(yourAnswer.toString())

            val answer = StringBuilder()

            for (i in 0 until questions[number].answers.size) answer.append(questions[number].answers[i]?.selection).append(" ")

            play_review_view.setTextAnswer(questions[number].answer)

        }

    }

    private fun actionCorrect() {

        soundRight.playSe()

        playViewModel.updateCorrect(questions[number], true)

        showImageJudge(R.drawable.right)

        if (sharedPreferenceManager.alwaysReview && questions[number].explanation != "") {//正解時も解説を表示

            showLayoutMistake("")

            play_review_view.setTextAnswer(questions[number].answer)

        } else {

            loadNext(600)

        }
    }

    private fun actionMistake(yourAnswer: String) {

        playViewModel.updateCorrect(questions[number], false)

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

    fun loadNext(second: Long) {

        runBlocking {
            delay(second)

            number += 1

            if (questions.size > number) {

                val question = questions[number]

                playViewModel.updateSolving(question, true)

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

    private fun showProblem(question: Quest) {

        play_problem_view.setTextProblem(question.getProblem(isReverse(question)))
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

    private fun showImageProblem(question: Quest) {

        if (question.imagePath == "") {

            play_problem_view.hideImage()

        } else {

            play_problem_view.showImage()

            if (question.imagePath.contains("/")) {

                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference.child(question.imagePath)

                play_problem_view.setImage(storageRef)

            } else {

                playViewModel.loadImage(question.imagePath) {
                    play_problem_view.getImageProblem()?.setImageWithGlide(baseContext, it)
                }

            }
        }
    }

    private fun showResult() {

        val i = Intent(this@PlayActivity, ResultActivity::class.java)

        i.putExtra("duration", System.currentTimeMillis() - startTime)
        i.putExtra("testId", playViewModel.testId)

        startActivity(i)

    }

    private fun showLayoutWrite() {

        if (sharedPreferenceManager.manual) {

            button_confirm.visibility = View.VISIBLE

            return

        }

        play_write_view.show()

    }

    private fun showLayoutSelect(question: Quest) {

        play_select_view.show(question)
        play_select_view.setTextChoices(question, makeChoice(question.selections.size))

    }

    private fun showLayoutComplete(question: Quest) {

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

    private fun showLayoutSelectComplete(question: Quest) {

        play_select_complete_view.show(question)
        play_select_complete_view.setTextChoices(question, makeChoice(question.selections.size))

    }

    private fun makeChoice(num: Int): ArrayList<String> {

        if (allAnswers.isEmpty()) {
            questions.forEach { q ->

                when (q.type) {
                    Constants.WRITE -> allAnswers.add(q.answer)
                    Constants.SELECT -> allAnswers.add(q.answer)
                    Constants.COMPLETE -> allAnswers = ArrayList(allAnswers.plus(q.answers.map { it.selection }))
                    Constants.SELECT_COMPLETE -> allAnswers = ArrayList(allAnswers.plus(q.answers.map { it.selection }))
                }
            }
            allAnswers = ArrayList(allAnswers.distinct())
        }

        val other = ArrayList<String>()
        val answers = ArrayList(allAnswers.map { it })

        var i = 0
        while (i < num) {

            if (answers.size > 0) {

                val rnd = Random()
                val ran = rnd.nextInt(answers.size)

                if (answers[ran] == questions[number].answer) {
                    answers.removeAt(ran)
                    continue
                }

                if (questions[number].answers.map { it.selection }.contains(answers[ran])) {
                    answers.removeAt(ran)
                    continue
                }

                other.add(answers[ran])
                answers.removeAt(ran)
                i++


            } else {
                other.add(i, getString(R.string.message_not_auto))
                i++
            }

        }

        return other
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

                playViewModel.updateCorrect(questions[number], true)

                loadNext(60)
            }

            override fun onClickMistake() {

                playViewModel.updateCorrect(questions[number], false)

                loadNext(60)

            }
        })

        button_confirm.setOnClickListener {

            button_confirm.isEnabled = false

            showLayoutManual()

            Handler().postDelayed({ button_confirm.isEnabled = true }, 600)

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

        play_review_view.setTextAnswer(question.getAnswer(isReverse(question)))

        play_review_view.setTextExplanation(question.explanation)

    }

    private fun isReverse(question: Quest): Boolean {

        return (question.type == Constants.WRITE || question.type == Constants.COMPLETE) && sharedPreferenceManager.reverse

    }
}
