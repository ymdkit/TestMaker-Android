package jp.gr.java_conf.foobar.testmaker.service.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.res.ResourcesCompat
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import jp.gr.java_conf.foobar.testmaker.service.Constants
import jp.gr.java_conf.foobar.testmaker.service.R
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadImage
import jp.gr.java_conf.foobar.testmaker.service.models.Quest
import jp.gr.java_conf.foobar.testmaker.service.models.SePlayer
import jp.gr.java_conf.foobar.testmaker.service.views.*
import kotlinx.android.synthetic.main.activity_play.*
import java.util.*

/**
 * Created by keita on 2016/07/17.
 */
class PlayActivity : BaseActivity() {

    internal var number: Int = 0

    private var testId: Long = 0

    private lateinit var soundMistake: SePlayer
    private lateinit var soundRight: SePlayer

    internal lateinit var questions: ArrayList<Quest>

    private lateinit var inputMethodManager: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        sendScreen("PlayActivity")

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        initToolBar()

        testId = intent.getLongExtra("testId", -1)

        val container = findViewById<LinearLayout>(R.id.container)
        container.addView(createAd())

        soundMistake = SePlayer(applicationContext, R.raw.mistake)
        soundRight = SePlayer(applicationContext, R.raw.correct)

        initViews()

        initQuestions()

        number = -1

        loadNext(0)

    }

    private fun initQuestions() {

        questions = if (intent.hasExtra("redo")) realmController.getQuestionsSolved(testId)
        else realmController.getQuestions(testId)

        if (sharedPreferenceManager.refine) {

            if (intent.hasExtra("redo")) {

                questions = ArrayList()

                for (question in realmController.getQuestionsSolved(testId)) {
                    if (!question.correct) {
                        questions.add(question)
                    } else {
                        realmController.updateSolving(question, false)
                    }
                }

            } else {

                realmController.updateSolving(questions, false)

                questions = ArrayList()

                for (question in realmController.getQuestions(testId))
                    if (!question.correct) questions.add(question)
            }

        } else {

            realmController.updateSolving(questions, false)

        }

        if (intent.hasExtra("random")) {

            questions.shuffle()
        }

        if (realmController.getTest(testId).limit < questions.size) {

            val temp = ArrayList<Quest>()

            for (i in 0 until realmController.getTest(testId).limit) {
                temp.add(questions[i])
            }

            questions = temp

        }

        realmController.updateSolving(questions, true)

    }

    override fun onPause() {

        play_write_view.hideKeyboard()

        inputMethodManager.hideSoftInputFromWindow(play_complete_view.firstEditText.windowToken, 0)

        super.onPause()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.action_rev) {
            val uri = Uri
                    .parse("https://play.google.com/store/apps/details?id=jp.gr.java_conf.foobar.testmaker.service&amp;hl=ja")
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            return true

        } else if (item.itemId == android.R.id.home) {
            startActivity(Intent(this@PlayActivity, MainActivity::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    fun checkAnswer(answer: String) {

        play_select_view.invalidate()

        val question = questions[number]

        if (answer == question.getAnswer(isReverse(question))) {

            actionCorrect()

        } else {

            actionMistake(answer)

            play_review_view.setTextAnswer(question.getAnswer(isReverse(question)))

        }

    }

    fun checkAnswer(answers: Array<String?>) { //完答

        var loop = false

        for (answer in answers) {

            loop = false
            for (k in 0 until questions[number].selections.size) {

                if (answer == questions[number].selections[k].selection) {
                    loop = true
                }
            }

            if (!loop) {
                break
            }

        }

        if (loop) {

            actionCorrect()

        } else {

            val yourAnswer = StringBuilder()
            for (your in answers) {
                if (your != "") {
                    yourAnswer.append(your).append(" ")
                }
            }

            actionMistake(yourAnswer.toString())

            val answer = StringBuilder()

            for (i in 0 until questions[number].selections.size) {
                answer.append(questions[number].selections[i].selection).append(" ")
            }

            play_review_view.setTextAnswer(answer.toString())

        }

    }

    private fun actionCorrect() {

        soundRight.playSe()

        realmController.updateCorrect(questions[number], true)

        showImageJudge(R.drawable.right)

        loadNext(600)

    }

    private fun actionMistake(yourAnswer: String) {

        realmController.updateCorrect(questions[number], false)

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

        play_review_view.visibility = View.VISIBLE
        play_review_view.setTextExplanation(questions[number].explanation)
        play_mistake_view.show(yourAnswer)

    }


    fun loadNext(second: Int) {

        Handler().postDelayed({

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
                }

            } else { //全問終了後

                showResult()

            }

        }, second.toLong())

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

        showImageProblem(question)

    }

    private fun showImageProblem(question: Quest) {

        if (question.imagePath == "") {

            play_problem_view.hideImage()

        } else {

            play_problem_view.showImage()

            play_problem_view.initImage()

            AsyncLoadImage(applicationContext, play_problem_view.getImageProblem(), question.imagePath, 1).execute(null)

        }
    }

    private fun showResult() {

        play_problem_view.initImage()

        val i = Intent(this@PlayActivity, ResultActivity::class.java)

        if (intent.hasExtra("random")) {
            i.putExtra("random", intent.getIntExtra("random", -1))
        }

        i.putExtra("testId", testId)

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

    private fun makeChoice(num: Int): ArrayList<String> {

        val other = ArrayList<String>()

        val answers = ArrayList<String>()

        val quests = realmController.getQuestions(testId)

        for (i in quests.indices) {
            if (quests[i].type != 2) {
                answers.add(quests[i].answer)
            }
        }

        var i = 0

        while (i < num) {

            if (answers.size > 0) {

                val rnd = Random()
                val ran = rnd.nextInt(answers.size)

                if (answers[ran] == questions[number].answer) {
                    answers.removeAt(ran)

                } else {
                    other.add(answers[ran])
                    answers.removeAt(ran)
                    i++
                }

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
                checkAnswer(play_complete_view.getAnswers(questions[number].selections.size))
            }
        })

        play_mistake_view.setOnClickListener(object : PlayMistakeView.OnClickListener {

            override fun onClick() {
                loadNext(0)
            }

        })

        play_manual_view.setOnClickListener(object : PlayManualView.OnClickListener {
            override fun onClickRight() {

                realmController.updateCorrect(questions[number], true)

                loadNext(60)
            }

            override fun onClickMistake() {

                realmController.updateCorrect(questions[number], false)

                loadNext(60)

            }
        })


        button_confirm.setOnClickListener {

            button_confirm.isEnabled = false

            showLayoutManual()

            Handler().postDelayed({ button_confirm.isEnabled = true }, 600)

        }

        if (Build.VERSION.SDK_INT >= 21) button_confirm.stateListAnimator = null

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
