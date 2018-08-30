package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.Random;

import jp.gr.java_conf.foobar.testmaker.service.Constants;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayCompleteView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayProblemView;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadImage;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.models.SePlayer;
import jp.gr.java_conf.foobar.testmaker.service.views.PlayReviewView;
import jp.gr.java_conf.foobar.testmaker.service.views.PlaySelectView;

/**
 * Created by keita on 2016/07/17.
 */
public class PlayActivity extends BaseActivity {

    int number;

    long testId;

    SePlayer soundMistake;
    SePlayer soundRight;

    ArrayList<Quest> questions;

    EditText editAnswer;
    TextView textYourAnswer;
    Button buttonJudge;

    RelativeLayout layoutWrite;
    TextInputLayout layoutWriteOne;
    LinearLayout layoutMiss;
    LinearLayout layoutMistake;
    LinearLayout layoutManual;
    LinearLayout layoutBody;

    PlayProblemView playProblemView;
    PlayCompleteView playCompleteView;
    PlaySelectView playSelectView;
    PlayReviewView playReviewView;

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

            questions = realmController.getQuestionsSolved(testId);

        } else {

            questions = realmController.getQuestions(testId);

        }

        if (sharedPreferenceManager.isRefine()) {

            if (getIntent().hasExtra("redo")) {

                questions = new ArrayList<>();

                for (Quest question : realmController.getQuestionsSolved(testId)) {
                    if (!question.getCorrect()) {
                        questions.add(question);
                    } else {
                        realmController.updateSolving(question, false);
                    }
                }

            } else {

                realmController.updateSolving(questions, false);

                questions = new ArrayList<>();

                for (Quest question : realmController.getQuestions(testId)) {
                    if (!question.getCorrect()) {
                        questions.add(question);
                    }
                }
            }

        } else {

            realmController.updateSolving(questions, false);

        }

        if (getIntent().hasExtra("random")) {

            Collections.shuffle(questions);
        }

        if (realmController.getTest(testId).getLimit() < questions.size()) {

            ArrayList<Quest> temp = new ArrayList<>();

            for (int i = 0; i < realmController.getTest(testId).getLimit(); i++) {
                temp.add(questions.get(i));
            }

            questions = temp;

        }

        realmController.updateSolving(questions, true);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        inputMethodManager.hideSoftInputFromWindow(editAnswer.getWindowToken(), 0);

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

        String answerOrigin;

        if (sharedPreferenceManager.isReverse() && (questions.get(number).getType() == Constants.WRITE || questions.get(number).getType() == Constants.COMPLETE)) {
            answerOrigin = questions.get(number).getProblem();
        } else {
            answerOrigin = questions.get(number).getAnswer();
        }

        playSelectView.invalidate();

        if (answer.equals(answerOrigin)) {

            actionCorrect();

        } else {

            actionMistake(answer);

            playReviewView.setTextAnswer(questions.get(number).getAnswer());

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

            playReviewView.setTextAnswer(questions.get(number).getAnswer());

        }

    }

    void actionCorrect() {

        soundRight.playSe();

        realmController.updateCorrect(questions.get(number), true);

        final ImageView right = findViewById(R.id.right);
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();

        if (lang.equals("ja")) {
            right.setImageDrawable(getResources().getDrawable(R.drawable.maru));

        } else {

            right.setImageDrawable(getResources().getDrawable(R.drawable.correct_eng));
        }
        right.setVisibility(View.VISIBLE);
        right.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));

        new Handler().postDelayed(() -> right.setVisibility(View.GONE), 600);

        loadNext(600);

    }

    void actionMistake(String yourAnswer) {

        realmController.updateCorrect(questions.get(number), false);

        showLayoutMistake(yourAnswer);

        final ImageView right = findViewById(R.id.right);
        Locale locale = Locale.getDefault();
        String lang = locale.getLanguage();
        if (lang.equals("ja")) {
            right.setImageDrawable(getResources().getDrawable(R.drawable.batsu));

        } else {
            right.setImageDrawable(getResources().getDrawable(R.drawable.incorrect_eng));
        }
        right.setVisibility(View.VISIBLE);
        right.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));

        new Handler().postDelayed(() -> right.setVisibility(View.GONE), 600);

        soundMistake.playSe();
    }

    private void showLayoutMistake(String yourAnswer) {

        layoutWrite.setVisibility(View.GONE);
        layoutMiss.setVisibility(View.VISIBLE);
        layoutMiss.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));

        playSelectView.hide();
        playReviewView.show();

        layoutManual.setVisibility(View.GONE);
        layoutMistake.setVisibility(View.VISIBLE);

        playReviewView.setTextExplanation(questions.get(number).getExplanation());


        if (!yourAnswer.equals("")) {
            textYourAnswer.setVisibility(View.VISIBLE);
            textYourAnswer.setText(getString(R.string.your_answer, yourAnswer));
        } else {
            textYourAnswer.setVisibility(View.GONE);
            textYourAnswer.setText("");
        }

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

        if (sharedPreferenceManager.isReverse() && (questions.get(number).getType() == Constants.WRITE || questions.get(number).getType() == Constants.COMPLETE)) {

            playProblemView.setTextProblem(questions.get(number).getAnswer());

        } else {

            playProblemView.setTextProblem(questions.get(number).getProblem());

        }

        playProblemView.setTextNumber(getString(R.string.number, String.valueOf(number + 1)));

        playReviewView.hide();

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

        layoutWrite.setVisibility(View.VISIBLE);
        layoutMiss.setVisibility(View.GONE);

        playSelectView.hide();

        layoutWriteOne.setVisibility(View.VISIBLE);

        playCompleteView.hide();

        editAnswer.setText("");
        editAnswer.setFocusable(true);
        editAnswer.requestFocus();

    }

    private void showLayoutSelect(Quest question) {
        layoutWrite.setVisibility(View.GONE);
        layoutMiss.setVisibility(View.GONE);

        playSelectView.show(question);


        playSelectView.setTextChoices(question, makeChoice(question.getSelections().size()));

    }

    private void showLayoutComplete(Quest question) {
        layoutWrite.setVisibility(View.VISIBLE);
        layoutMiss.setVisibility(View.GONE);

        playSelectView.hide();

        if (sharedPreferenceManager.isReverse()) {

            layoutWriteOne.setVisibility(View.VISIBLE);

            playCompleteView.hide();

        } else {

            layoutWriteOne.setVisibility(View.GONE);

            playCompleteView.show();
        }

        playCompleteView.initEditAnswers(question);

    }

    ArrayList<String> makeChoice(int num) {
        ArrayList<String> other = new ArrayList<>();

        ArrayList<String> answers = new ArrayList<>();

        ArrayList<Quest> quests = realmController.getQuestions(testId);

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

        textYourAnswer = findViewById(R.id.text_your_answer);
        layoutBody = findViewById(R.id.body);
        layoutWrite = findViewById(R.id.layout_write);
        layoutWriteOne = findViewById(R.id.textInputLayout_answer);
        layoutMistake = findViewById(R.id.layout_mistake);
        layoutManual = findViewById(R.id.layout_manual);
        layoutMiss = findViewById(R.id.layout_miss);
        editAnswer = findViewById(R.id.set_answer);

        playProblemView = findViewById(R.id.play_problem_view);
        playCompleteView = findViewById(R.id.play_complete_view);
        playSelectView = findViewById(R.id.play_select_view);
        playReviewView = findViewById(R.id.play_review_view);

        playSelectView.setOnClickListener((PlaySelectView.OnClickListener) this::checkAnswer);

        editAnswer.clearFocus();
        editAnswer.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // ソフトキーボードを表示する
                inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_FORCED);
            }
            // フォーカスが外れたとき
            else {
                // ソフトキーボードを閉じる
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        });

        buttonJudge = findViewById(R.id.judge);

        if (sharedPreferenceManager.isManual()) {

            editAnswer.setVisibility(View.GONE);
            buttonJudge.setText(getString(R.string.confirm));

            playCompleteView.hide();

        }

        buttonJudge.setOnClickListener(v -> {
            buttonJudge.setEnabled(false);

            Quest question = questions.get(number);

            if (sharedPreferenceManager.isManual()) {

                showLayoutManual();

            } else {

                switch (question.getType()) {
                    case Constants.WRITE:
                        checkAnswer(String.valueOf(editAnswer.getText()));
                        break;
                    case Constants.COMPLETE:

                        if (sharedPreferenceManager.isReverse()) {

                            checkAnswer(String.valueOf(editAnswer.getText()));

                        } else {

                            checkAnswer(playCompleteView.getAnswers(question.getSelections().size()));
                        }


                        break;
                }

            }

            new Handler().postDelayed(() -> buttonJudge.setEnabled(true), 600);

        });

        Button right_self = findViewById(R.id.button_right);
        right_self.setOnClickListener(view -> {

            realmController.updateCorrect(questions.get(number), true);

            loadNext(60);

        });
        Button mistake_self = findViewById(R.id.button_mistake);
        mistake_self.setOnClickListener(view -> {
            realmController.updateCorrect(questions.get(number), false);

            loadNext(60);
        });

        Button next = findViewById(R.id.next);

        if (Build.VERSION.SDK_INT >= 21) {
            buttonJudge.setStateListAnimator(null);
            mistake_self.setStateListAnimator(null);
            right_self.setStateListAnimator(null);
            next.setStateListAnimator(null);
        }

        next.setOnClickListener(v -> loadNext(0));
    }

    private void showLayoutManual() {

        layoutWrite.setVisibility(View.GONE);
        layoutMiss.setVisibility(View.VISIBLE);
        layoutMiss.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));
        layoutManual.setVisibility(View.VISIBLE);
        layoutMistake.setVisibility(View.GONE);

        playSelectView.hide();
        playReviewView.show();

        String answerOrigin;

        if (sharedPreferenceManager.isReverse()) {
            answerOrigin = questions.get(number).getProblem();
        } else {
            answerOrigin = questions.get(number).getAnswer();
        }

        playReviewView.setTextAnswer(getString(R.string.message_answer, answerOrigin));

        playReviewView.setTextExplanation(questions.get(number).getExplanation());

    }

}
