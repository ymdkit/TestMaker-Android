package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import jp.gr.java_conf.foobar.testmaker.service.Constants;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadImage;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.models.SePlayer;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayCompleteView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayManualView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayMistakeView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayProblemView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayReviewView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlaySelectView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayWriteView;

/**
 * Created by keita on 2016/07/17.
 */
public class PlayActivity extends BaseActivity {

    int number;

    long testId;

    SePlayer soundMistake;
    SePlayer soundRight;

    ArrayList<Quest> questions;

    PlayProblemView playProblemView;
    PlayReviewView playReviewView;
    PlayMistakeView playMistakeView;
    PlayManualView playManualView;
    PlayWriteView playWriteView;
    PlayCompleteView playCompleteView;
    PlaySelectView playSelectView;
    Button buttonConfirm;
    ImageView imageJudge;

    InputMethodManager inputMethodManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        sendScreen("PlayActivity");

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initToolBar();

        testId = getIntent().getLongExtra("testId", -1);

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        soundMistake = new SePlayer(getApplicationContext(), R.raw.mistake);
        soundRight = new SePlayer(getApplicationContext(), R.raw.correct);

        initViews();

        initQuestions();

        number = -1;

        loadNext(0);

    }

    private void initQuestions() {

        if (getIntent().hasExtra("redo")) {

            questions = getRealmController().getQuestionsSolved(testId);

        } else {

            questions = getRealmController().getQuestions(testId);

        }

        if (getSharedPreferenceManager().getRefine()) {

            if (getIntent().hasExtra("redo")) {

                questions = new ArrayList<>();

                for (Quest question : getRealmController().getQuestionsSolved(testId)) {
                    if (!question.getCorrect()) {
                        questions.add(question);
                    } else {
                        getRealmController().updateSolving(question, false);
                    }
                }

            } else {

                getRealmController().updateSolving(questions, false);

                questions = new ArrayList<>();

                for (Quest question : getRealmController().getQuestions(testId)) {
                    if (!question.getCorrect()) {
                        questions.add(question);
                    }
                }
            }

        } else {

            getRealmController().updateSolving(questions, false);

        }

        if (getIntent().hasExtra("random")) {

            Collections.shuffle(questions);
        }

        if (getRealmController().getTest(testId).getLimit() < questions.size()) {

            ArrayList<Quest> temp = new ArrayList<>();

            for (int i = 0; i < getRealmController().getTest(testId).getLimit(); i++) {
                temp.add(questions.get(i));
            }

            questions = temp;

        }

        getRealmController().updateSolving(questions, true);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {

        playWriteView.hideKeyboard();

        inputMethodManager.hideSoftInputFromWindow(playCompleteView.getFirstEditText().getWindowToken(), 0);

        super.onPause();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_rev) {
            Uri uri = Uri
                    .parse("https://play.google.com/store/apps/details?id=jp.gr.java_conf.foobar.testmaker.service&amp;hl=ja");
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
            return true;

        } else if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(PlayActivity.this, MainActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void checkAnswer(String answer) {

        playSelectView.invalidate();

        Quest question = questions.get(number);

        if (answer.equals(question.getAnswer(isReverse(question)))) {

            actionCorrect();

        } else {

            actionMistake(answer);

            playReviewView.setTextAnswer(question.getAnswer(isReverse(question)));

        }

    }

    void checkAnswer(String[] answers) { //完答

        boolean loop = false;

        for (String answer : answers) {

            loop = false;
            for (int k = 0; k < questions.get(number).getSelections().size(); k++) {

                if (answer.equals(questions.get(number).getSelections().get(k).getSelection())) {
                    loop = true;
                }
            }

            if (!loop) {
                break;
            }

        }

        if (loop) {

            actionCorrect();

        } else {

            StringBuilder yourAnswer = new StringBuilder();
            for (String your : answers) {
                if (!your.equals("")) {
                    yourAnswer.append(your).append(" ");
                }
            }

            actionMistake(yourAnswer.toString());

            StringBuilder answer = new StringBuilder();

            for (int i = 0; i < questions.get(number).getSelections().size(); i++) {
                answer.append(questions.get(number).getSelections().get(i).getSelection()).append(" ");
            }

            playReviewView.setTextAnswer(answer.toString());

        }

    }

    void actionCorrect() {

        soundRight.playSe();

        getRealmController().updateCorrect(questions.get(number), true);

        showImageJudge(R.drawable.right);

        loadNext(600);

    }

    void actionMistake(String yourAnswer) {

        getRealmController().updateCorrect(questions.get(number), false);

        showLayoutMistake(yourAnswer);

        showImageJudge(R.drawable.mistake);

        soundMistake.playSe();
    }

    private void showImageJudge(int id) {

        imageJudge.setImageDrawable(getResources().getDrawable(id));
        imageJudge.setVisibility(View.VISIBLE);
        imageJudge.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));

        new Handler().postDelayed(() -> imageJudge.setVisibility(View.GONE), 600);

    }

    private void showLayoutMistake(String yourAnswer) {

        playWriteView.setVisibility(View.GONE);
        playCompleteView.setVisibility(View.GONE);
        playSelectView.setVisibility(View.GONE);

        playReviewView.setVisibility(View.VISIBLE);
        playReviewView.setTextExplanation(questions.get(number).getExplanation());
        playMistakeView.show(yourAnswer);

    }


    void loadNext(final int second) {

        new Handler().postDelayed(() -> {

            number += 1;

            if (questions.size() > number) {

                Quest question = questions.get(number);

                showProblem(question);

                switch (question.getType()) {

                    case Constants.WRITE:

                        showLayoutWrite();

                        break;
                    case Constants.COMPLETE:

                        showLayoutComplete(question);

                        break;

                    case Constants.SELECT:

                        showLayoutSelect(question);

                        break;
                }

            } else { //全問終了後

                showResult();

            }

        }, second);

    }

    private void showProblem(Quest question) {

        playProblemView.setTextProblem(question.getProblem(isReverse(question)));

        playProblemView.setTextNumber(getString(R.string.number, String.valueOf(number + 1)));

        playReviewView.setVisibility(View.GONE);
        playMistakeView.setVisibility(View.GONE);
        playManualView.setVisibility(View.GONE);
        playWriteView.setVisibility(View.GONE);
        playCompleteView.setVisibility(View.GONE);
        playSelectView.setVisibility(View.GONE);

        showImageProblem(question);

    }

    private void showImageProblem(Quest question) {

        if (question.getImagePath().equals("")) {

            playProblemView.hideImage();

        } else {

            playProblemView.showImage();

            playProblemView.initImage();

            new AsyncLoadImage(getApplicationContext(), playProblemView.getImageProblem(), question.getImagePath(), 1).execute((Bitmap[]) null);

        }
    }

    private void showResult() {

        playProblemView.initImage();

        Intent i = new Intent(PlayActivity.this, ResultActivity.class);

        if (getIntent().hasExtra("random")) {
            i.putExtra("random", getIntent().getIntExtra("random", -1));
        }

        i.putExtra("testId", testId);

        startActivity(i);

    }

    private void showLayoutWrite() {

        if(getSharedPreferenceManager().getManual()){

            buttonConfirm.setVisibility(View.VISIBLE);

            return;

        }

        playWriteView.show();

    }

    private void showLayoutSelect(Quest question) {

        playSelectView.show(question);
        playSelectView.setTextChoices(question, makeChoice(question.getSelections().size()));

    }

    private void showLayoutComplete(Quest question) {

        if(getSharedPreferenceManager().getManual()){

            buttonConfirm.setVisibility(View.VISIBLE);

            return;

        }

        if (getSharedPreferenceManager().getReverse()) {

            playWriteView.show();

        } else {

            playCompleteView.setVisibility(View.VISIBLE);

            playCompleteView.initEditAnswers(question);

        }

    }

    ArrayList<String> makeChoice(int num) {

        ArrayList<String> other = new ArrayList<>();

        ArrayList<String> answers = new ArrayList<>();

        ArrayList<Quest> quests = getRealmController().getQuestions(testId);

        for (int i = 0; i < quests.size(); i++) {
            if (quests.get(i).getType() != 2) {
                answers.add(quests.get(i).getAnswer());
            }
        }

        int i = 0;

        while (i < num) {

            if (answers.size() > 0) {

                Random rnd = new Random();
                int ran = rnd.nextInt(answers.size());

                if (answers.get(ran).equals(questions.get(number).getAnswer())) {
                    answers.remove(ran);

                } else {
                    other.add(answers.get(ran));
                    answers.remove(ran);
                    i++;
                }

            } else {
                other.add(i, getString(R.string.message_not_auto));
                i++;
            }

        }

        return other;
    }

    void initViews() {

        playProblemView = findViewById(R.id.play_problem_view);
        playWriteView = findViewById(R.id.play_write_view);
        playCompleteView = findViewById(R.id.play_complete_view);
        playSelectView = findViewById(R.id.play_select_view);
        playReviewView = findViewById(R.id.play_review_view);
        playMistakeView = findViewById(R.id.play_mistake_view);
        playManualView = findViewById(R.id.play_manual_view);
        imageJudge = findViewById(R.id.image_judge);

        playWriteView.setOnClickListener((PlayWriteView.OnClickListener) this::checkAnswer);

        playSelectView.setOnClickListener((PlaySelectView.OnClickListener) this::checkAnswer);

        playCompleteView.setOnClickListener(() -> checkAnswer(playCompleteView.getAnswers(questions.get(number).getSelections().size())));

        playMistakeView.setOnClickListener(() -> loadNext(0));

        playManualView.setOnClickListener(new PlayManualView.OnClickListener() {
            @Override
            public void onClickRight() {

                getRealmController().updateCorrect(questions.get(number), true);

                loadNext(60);
            }

            @Override
            public void onClickMistake() {

                getRealmController().updateCorrect(questions.get(number), false);

                loadNext(60);

            }
        });


        buttonConfirm = findViewById(R.id.button_confirm);
        buttonConfirm.setOnClickListener(v -> {

            buttonConfirm.setEnabled(false);

            showLayoutManual();

            new Handler().postDelayed(() -> buttonConfirm.setEnabled(true), 600);

        });

        if (Build.VERSION.SDK_INT >= 21) {
            buttonConfirm.setStateListAnimator(null);
        }

    }

    private void showLayoutManual() {

        playWriteView.setVisibility(View.GONE);
        playSelectView.setVisibility(View.GONE);
        playCompleteView.setVisibility(View.GONE);
        playReviewView.setVisibility(View.VISIBLE);
        playManualView.setVisibility(View.VISIBLE);
        buttonConfirm.setVisibility(View.GONE);

        Quest question = questions.get(number);

        playReviewView.setTextAnswer(question.getAnswer(isReverse(question)));

        playReviewView.setTextExplanation(question.getExplanation());

    }

    public boolean isReverse(Quest question) {

        return (question.getType() == Constants.WRITE || question.getType() == Constants.COMPLETE) && getSharedPreferenceManager().getReverse();

    }
}
