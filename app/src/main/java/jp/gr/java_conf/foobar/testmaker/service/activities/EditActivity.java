package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.isseiaoki.simplecropview.CropImageView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Calendar;

import jp.gr.java_conf.foobar.testmaker.service.Constants;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadImage;
import jp.gr.java_conf.foobar.testmaker.service.models.CategoryEditor;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.models.StructQuestion;
import jp.gr.java_conf.foobar.testmaker.service.views.ColorChooser;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.EditAdapter;

/**
 * Created by keita on 2017/02/12.
 */

public class EditActivity extends BaseActivity {
    private static final int REQUEST_PICK_IMAGE = 10011;
    private static final int REQUEST_SAF_PICK_IMAGE = 10012;

    EditAdapter editAdapter;
    RecyclerView recyclerView;

    RelativeLayout editChoose;
    TextInputLayout editWrite;
    TextInputLayout editExplanation;
    RelativeLayout editWriteAnd;
    LinearLayout body;
    ImageButton expand;
    Button buttonAdd;
    Button buttonCancel;
    Button buttonType;
    ImageButton buttonImage;
    EditText textProblem;
    EditText textAnswerWrite;
    EditText textAnswerChoose;
    EditText textExplanation;
    EditText[] answers;
    EditText[] others;
    TextView textTitle;
    SearchView searchView;
    boolean auto;
    boolean explanation;

    int typeQuestion;

    String imagePath;
    long testId;
    long questionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        sendScreen("EditActivity");

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        initToolBar();

        imagePath = "";
        testId = getIntent().getLongExtra("testId", -1);
        questionId = -1;

        auto = getSharedPreferenceManager().getAuto();

        explanation = getSharedPreferenceManager().getExplanation();

        initAdapter();

        initViews();

    }

    private void initAdapter() {

        editAdapter = new EditAdapter(this, getRealmController(), testId);

        editAdapter.setOnClickListener(new EditAdapter.OnClickListener() {
            @Override
            public void onClickEditQuestion(int position) {

                showLayoutEdit();

                textTitle.setText(getString(R.string.edit_question));
                buttonCancel.setVisibility(View.VISIBLE);
                buttonAdd.setText(getString(R.string.save_question));

                Quest question;

                if (editAdapter.getFilter()) {
                    question = getRealmController().getFilterQuestions(testId, editAdapter.getSearchWord()).get(position);
                } else {
                    question = getRealmController().getQuestion(testId, position);

                }

                textProblem.setText(question.getProblem());
                questionId = question.getId();

                if (!question.getImagePath().equals("")) {

                    imagePath = question.getImagePath();
                    AsyncLoadImage task = new AsyncLoadImage(getApplicationContext(), buttonImage, imagePath, 1);
                    task.execute((Bitmap[]) null);

                } else {

                    buttonImage.setImageResource(R.drawable.ic_photo_white);
                }

                if (!question.getExplanation().equals("")) {

                    editExplanation.setVisibility(View.VISIBLE);
                    textExplanation.setText(question.getExplanation());

                } else {

                    editExplanation.setVisibility(View.GONE);

                }

                switch (question.getType()) {

                    case 0:
                        showLayoutWrite();

                        textAnswerWrite.setText(question.getAnswer());

                        getSharedPreferenceManager().setNumWrite(1);

                        buttonType.setText(getString(R.string.action_choose));

                        break;
                    case 1:

                        showLayoutSelect();

                        reload_others(question.getSelections().size());
                        getSharedPreferenceManager().setNumChoose(question.getSelections().size());

                        textAnswerChoose.setText(question.getAnswer());

                        for (int i = 0; i < question.getSelections().size(); i++) {
                            others[i].setText(question.getSelections().get(i).getSelection());
                        }

                        buttonType.setText(getString(R.string.action_write));

                        getSharedPreferenceManager().setAuto(question.getAuto());

                        if (getSharedPreferenceManager().getAuto()) {
                            auto(getSharedPreferenceManager().getNumChoose());
                        } else {
                            offAuto(getSharedPreferenceManager().getNumChoose());
                        }

                        break;
                    case 2:
                        showLayoutWriteComplete();
                        reload_answers(question.getSelections().size());

                        getSharedPreferenceManager().setNumWrite(question.getSelections().size());

                        for (int i = 0; i < question.getSelections().size(); i++) {
                            answers[i].setText(question.getSelections().get(i).getSelection());
                        }

                        buttonType.setText(getString(R.string.action_choose));

                        break;
                }

            }

            @Override
            public void onClickDeleteQuestion(Quest question) {

                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle(getString(R.string.delete_question));
                builder.setMessage(getString(R.string.message_delete, question.getProblem()));
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    if (!question.getImagePath().equals("")) {
                        deleteFile(question.getImagePath());
                    }

                    getRealmController().deleteQuestion(question);

                    editAdapter.notifyDataSetChanged();

                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();

            }
        });

    }

    private void showLayoutEdit() {

        body.setVisibility(View.VISIBLE);
        expand.setImageResource(R.drawable.ic_expand_less_black);
        textProblem.setFocusable(true);
        textProblem.requestFocus();
        buttonAdd.setVisibility(View.VISIBLE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        if (resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            final Uri uri;
            if (resultData != null) {

                try {

                    uri = resultData.getData();

                    final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_crop,
                            findViewById(R.id.layout_dialog_crop_image));

                    final CropImageView cropView = dialogLayout.findViewById(R.id.cropImageView);
                    cropView.setImageBitmap(getBitmapFromUri(uri));

                    AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this, R.style.MyAlertDialogStyle);
                    builder.setView(dialogLayout);
                    builder.setTitle(getString(R.string.trim));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setNegativeButton(android.R.string.cancel, null);


                    final AlertDialog dialog = builder.show();


                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    positiveButton.setOnClickListener(view -> {

                        imagePath = getFileName();

                        AsyncLoadImage task = new AsyncLoadImage(getApplicationContext(), buttonImage, imagePath, 0);
                        task.execute(cropView.getCroppedBitmap());

                        dialog.dismiss();
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    protected String getFileName() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) + "_" + (c.get(Calendar.MONTH) + 1) + "_" + c.get(Calendar.DAY_OF_MONTH) + "_" + c.get(Calendar.HOUR_OF_DAY) + "_" + c.get(Calendar.MINUTE) + "_" + c.get(Calendar.SECOND) + "_" + c.get(Calendar.MILLISECOND) + ".png";
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void cancelEditing() {

        hideLayoutEdit();

        buttonCancel.setVisibility(View.GONE);

        reset();

    }

    private void hideLayoutEdit() {

        body.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);

        expand.setImageResource(R.drawable.ic_expand_more_black);
        textTitle.setText(getString(R.string.add_question));

    }

    public void addQuestion() {

        switch (typeQuestion) {

            case Constants.WRITE:

                if ((String.valueOf(textProblem.getText()).equals("")) || (String.valueOf(textAnswerWrite.getText()).equals(""))) {

                    Toast.makeText(getApplicationContext(), getString(R.string.message_shortage), Toast.LENGTH_LONG).show();

                    return;

                } else {

                    StructQuestion p = new StructQuestion(textProblem.getText().toString(), String.valueOf(textAnswerWrite.getText()));

                    p.setImagePath(imagePath);
                    p.setExplanation(textExplanation.getText().toString());
                    getRealmController().addQuestion(testId, p, questionId);

                    reset();

                }
                break;

            case Constants.SELECT:

                if (String.valueOf(textProblem.getText()).equals("") || String.valueOf(textAnswerChoose.getText()).equals("")) {

                    Toast.makeText(getApplicationContext(), getString(R.string.message_shortage), Toast.LENGTH_LONG).show();

                    return;

                } else {

                    int j = 0;

                    for (EditText other : others) {

                        if (other.getVisibility() == View.VISIBLE && String.valueOf(other.getText()).equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.message_shortage), Toast.LENGTH_LONG).show();
                            return;
                        } else if (other.getVisibility() == View.VISIBLE) {
                            j++;
                        }
                    }

                    String[] strings = new String[j];

                    for (int i = 0; i < j; i++) {
                        strings[i] = String.valueOf(others[i].getText());
                    }

                    StructQuestion p = new StructQuestion(textProblem.getText().toString(), String.valueOf(textAnswerChoose.getText()), strings);
                    p.setAuto(getSharedPreferenceManager().getAuto());
                    p.setImagePath(imagePath);
                    p.setExplanation(textExplanation.getText().toString());
                    getRealmController().addQuestion(testId, p, questionId);

                    reset();

                }

                break;
            case Constants.COMPLETE:

                if (String.valueOf(textProblem.getText()).equals("")) {

                    Toast.makeText(getApplicationContext(), getString(R.string.message_shortage), Toast.LENGTH_LONG).show();

                    return;

                } else {

                    int k = 0;

                    for (EditText answer : answers
                            ) {
                        if (answer.getVisibility() == View.VISIBLE && String.valueOf(answer.getText()).equals("")) {
                            Toast.makeText(getApplicationContext(), getString(R.string.message_shortage), Toast.LENGTH_LONG).show();
                            return;
                        } else if (answer.getVisibility() == View.VISIBLE) {
                            k++;
                        }
                    }

                    String[] strings = new String[k];

                    for (int i = 0; i < strings.length; i++) {
                        strings[i] = String.valueOf(answers[i].getText());
                    }

                    StructQuestion p = new StructQuestion(textProblem.getText().toString(), strings);
                    p.setImagePath(imagePath);
                    p.setExplanation(textExplanation.getText().toString());

                    getRealmController().addQuestion(testId, p, questionId);

                    reset();
                }
                break;
        }

        editAdapter.notifyDataSetChanged();

        buttonCancel.setVisibility(View.GONE);

        if (editChoose.getVisibility() == View.GONE) {
            textTitle.setText(getString(R.string.add_question_write));
        } else {
            textTitle.setText(getString(R.string.add_question_choose));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                editAdapter.setSearchWord(s);

                editAdapter.setFilter(true);

                editAdapter.notifyDataSetChanged();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                editAdapter.setSearchWord(s);

                editAdapter.setFilter(true);

                editAdapter.notifyDataSetChanged();

                return false;
            }
        });

        searchView.setOnCloseListener(() -> {

            editAdapter.setSearchWord("");

            editAdapter.setFilter(false);

            editAdapter.notifyDataSetChanged();

            return false;
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int action_id = item.getItemId();

        if (action_id == R.id.action_setting) {

            final View dialogLayout = LayoutInflater.from(this).inflate(R.layout.dialog_edit_test,
                    findViewById(R.id.layout_dialog_edit_test));

            final EditText name = dialogLayout.findViewById(R.id.edit_title);

            final Button button_cate = dialogLayout.findViewById(R.id.button_category);

            final ColorChooser colorChooser = dialogLayout.findViewById(R.id.color_chooser);

            if (Build.VERSION.SDK_INT >= 21) {
                button_cate.setStateListAnimator(null);
            }

            button_cate.setTag(getRealmController().getTest(testId).getCategory());

            if (getRealmController().getTest(testId).getCategory().equals("")) {

                button_cate.setText(getString(R.string.category));
            } else {
                button_cate.setText(getRealmController().getTest(testId).getCategory());
            }

            button_cate.setOnClickListener(view -> {
                CategoryEditor categoryEditor = new CategoryEditor(EditActivity.this, button_cate, getRealmController(), null);
                categoryEditor.setCategory();
            });

            button_cate.setOnLongClickListener(view -> {

                // アラーとダイアログ を生成
                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this, R.style.MyAlertDialogStyle);
                builder.setMessage(getString(R.string.cancel_category));
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    button_cate.setTag("");
                    button_cate.setText(getString(R.string.category));
                    button_cate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_blue));
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();


                return false;
            });

            name.setText(getRealmController().getTest(testId).getTitle());

            colorChooser.setColorId(getRealmController().getTest(testId).getColor());

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            builder.setView(dialogLayout);
            builder.setTitle(getString(R.string.edit_exam));
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setNegativeButton(android.R.string.cancel, null);

            final AlertDialog dialog = builder.show();

            Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                // 場合によっては自分で明示的に閉じる必要がある
                SpannableStringBuilder sb = (SpannableStringBuilder) name.getText();

                if (sb.toString().equals("")) {

                    Toast.makeText(getApplicationContext(), getString(R.string.message_wrong), Toast.LENGTH_SHORT).show();

                } else {

                    getRealmController().updateTest(getRealmController().getTest(testId), sb.toString(), colorChooser.getColorId(), button_cate.getTag().toString());

                    dialog.dismiss();
                }
            });

            dialog.show();

        } else if (item.getItemId() == android.R.id.home) {

            finish();

            return true;
        } else if (item.getItemId() == R.id.action_edit_pro) {

            Intent i = new Intent(EditActivity.this, EditProActivity.class);

            i.putExtra("testId", testId);
            startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showLayoutWrite() {
        typeQuestion = Constants.WRITE;
        editWrite.setVisibility(View.VISIBLE);
        editWrite.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));
        editWriteAnd.setVisibility(View.GONE);
        editChoose.setVisibility(View.GONE);
    }

    public void showLayoutWriteComplete() {
        typeQuestion = Constants.COMPLETE;
        editWriteAnd.setVisibility(View.VISIBLE);
        editWriteAnd.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));
        editChoose.setVisibility(View.GONE);
        editWrite.setVisibility(View.GONE);
    }

    public void showLayoutSelect() {
        typeQuestion = Constants.SELECT;
        editWriteAnd.setVisibility(View.GONE);
        editWrite.setVisibility(View.GONE);
        editChoose.setVisibility(View.VISIBLE);
        editChoose.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.alpha_appear));
    }

    public void auto(int limit) {
        for (int i = 0; i < limit; i++) {
            others[i].setText(getString(R.string.state_auto));
            others[i].setEnabled(false);

        }
    }

    public void offAuto(int limit) {
        for (int i = 0; i < limit; i++) {
            if (others[i].getText().toString().equals(getString(R.string.state_auto))) {
                others[i].setText("");
            }

            others[i].setEnabled(true);

        }
    }

    public void reset() {

        textProblem.setText("");
        textProblem.requestFocus();
        textAnswerWrite.setText("");
        textAnswerChoose.setText("");
        textExplanation.setText("");
        questionId = -1;
        imagePath = "";
        buttonImage.setImageResource(R.drawable.ic_photo_white);
        buttonImage.setBackgroundResource(R.drawable.button_blue);

        buttonAdd.setText(getString(R.string.action_add));

        for (EditText t : others) {
            t.setText("");
        }

        for (EditText t : answers) {
            t.setText("");
        }

        if (getSharedPreferenceManager().getAuto()) {
            auto(getSharedPreferenceManager().getNumChoose());
        } else {
            offAuto(getSharedPreferenceManager().getNumChoose());
        }
    }

    public void reload_others(int num) {
        for (int i = 0; i < others.length; i++) {
            if (i < num) {
                others[i].setVisibility(View.VISIBLE);
            } else {
                others[i].setVisibility(View.GONE);
            }
        }
    }

    public void reload_answers(int num) {
        for (int i = 0; i < answers.length; i++) {
            if (i < num) {
                answers[i].setVisibility(View.VISIBLE);
            } else {
                answers[i].setVisibility(View.GONE);
            }
        }
    }

    public void initViews() {
        textTitle = findViewById(R.id.text_title);
        textProblem = findViewById(R.id.set_problem);
        textAnswerWrite = findViewById(R.id.set_answer_write);
        textAnswerChoose = findViewById(R.id.set_answer_choose);
        textExplanation = findViewById(R.id.set_explanation);

        others = new EditText[5];
        answers = new EditText[4];

        for (int i = 0; i < others.length; i++) {
            String s = "set_other" + String.valueOf(i + 1);
            int strId = getResources().getIdentifier(s, "id", getPackageName());
            others[i] = findViewById(strId);
        }

        for (int i = 0; i < answers.length; i++) {
            String s = "set_answer_write_" + String.valueOf(i + 1);
            int strId = getResources().getIdentifier(s, "id", getPackageName());
            answers[i] = findViewById(strId);
        }

        editChoose = findViewById(R.id.edit_choose);
        editExplanation = findViewById(R.id.textInputLayout_explanation);
        if (explanation) {
            editExplanation.setVisibility(View.VISIBLE);
        }
        editWrite = findViewById(R.id.textInputLayout_answer_write);
        editWriteAnd = findViewById(R.id.layout_answer_write_2);
        body = findViewById(R.id.layout_body);

        expand = findViewById(R.id.ImageButton_expand);
        expand.setOnClickListener(v -> {

            if (body.getVisibility() != View.GONE) {

                hideLayoutEdit();

            } else {

                showLayoutEdit();

                if (editChoose.getVisibility() == View.GONE) {
                    textTitle.setText(getString(R.string.add_question_write));
                } else {
                    textTitle.setText(getString(R.string.add_question_choose));
                }
            }

            reset();
        });

        final Button button_detail = findViewById(R.id.button_choose);
        button_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final View dialogLayout = LayoutInflater.from(EditActivity.this).inflate(R.layout.dialog_detail,
                        findViewById(R.id.layout_dialog_detail));

                final ImageButton add = dialogLayout.findViewById(R.id.add);
                final ImageButton minus = dialogLayout.findViewById(R.id.minus);

                final TextView number = dialogLayout.findViewById(R.id.size_choose);
                final SwitchCompat change_auto = dialogLayout.findViewById(R.id.change_auto);

                final SwitchCompat change_explanation = dialogLayout.findViewById(R.id.change_explanation);

                change_explanation.setChecked(getSharedPreferenceManager().getExplanation());
                change_explanation.setOnCheckedChangeListener((buttonView, isChecked) -> explanation = isChecked);

                change_auto.setChecked(getSharedPreferenceManager().getAuto());
                change_auto.setOnCheckedChangeListener((buttonView, isChecked) -> auto = isChecked);

                switch (typeQuestion) {
                    case 0:
                    case 2:

                        final LinearLayout e = dialogLayout.findViewById(R.id.layout_switch);
                        e.setVisibility(View.GONE);

                        final TextView t = dialogLayout.findViewById(R.id.textView);
                        t.setText(getString(R.string.number_answers));

                        number.setText(String.valueOf(getSharedPreferenceManager().getNumWrite()));

                        change_auto.setVisibility(View.GONE);

                        break;
                    case 1:

                        number.setText(String.valueOf(getSharedPreferenceManager().getNumChoose() + 1));

                        break;
                }

                add.setOnClickListener(v -> check_count(number, 1));

                minus.setOnClickListener(v -> check_count(number, -1));

                AlertDialog.Builder builder = new AlertDialog.Builder(EditActivity.this, R.style.MyAlertDialogStyle);
                builder.setView(dialogLayout);
                builder.setTitle(getString(R.string.action_detail));
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setNegativeButton(android.R.string.cancel, null);


                final AlertDialog dialog = builder.show();

                Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(v -> {
                    // 場合によっては自分で明示的に閉じる必要がある

                    getSharedPreferenceManager().setExplanation(explanation);
                    if (explanation) {
                        editExplanation.setVisibility(View.VISIBLE);
                    } else {
                        editExplanation.setVisibility(View.GONE);
                    }

                    switch (typeQuestion) {

                        case 0:
                        case 2:

                            reload_answers(Integer.parseInt(String.valueOf(number.getText())));
                            getSharedPreferenceManager().setNumWrite(Integer.parseInt(String.valueOf(number.getText())));

                            if (getSharedPreferenceManager().getNumWrite() > 1) {
                                showLayoutWriteComplete();
                            } else {
                                showLayoutWrite();
                            }
                            break;
                        case 1:
                            getSharedPreferenceManager().setAuto(auto);
                            reload_others(Integer.parseInt(String.valueOf(number.getText())) - 1);
                            getSharedPreferenceManager().setNumChoose(Integer.parseInt(String.valueOf(number.getText())) - 1);

                            if (auto) {
                                auto(getSharedPreferenceManager().getNumChoose());
                            } else {
                                offAuto(getSharedPreferenceManager().getNumChoose());
                            }

                            break;
                    }

                    dialog.dismiss();

                });

                dialog.show();
            }

            void check_count(TextView number, int i) {
                int num = Integer.parseInt(String.valueOf(number.getText()));

                int mini = 0;
                int max = 0;

                switch (typeQuestion) {
                    case 0:
                    case 2:
                        mini = 1;
                        max = 4;
                        break;
                    case 1:
                        mini = 2;
                        max = 6;
                        break;
                }

                if (num + i >= mini && num + i <= max) {
                    number.setText(String.valueOf(num + i));
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.limit, mini, max), Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonType = findViewById(R.id.button_write);
        buttonType.setOnClickListener(view -> {

            if (buttonType.getText().equals(getString(R.string.action_choose))) {

                showLayoutSelect();
                reload_others(getSharedPreferenceManager().getNumChoose());

                buttonType.setText(getString(R.string.action_write));
                textTitle.setText(getString(R.string.add_question_choose));

            } else {

                if (getSharedPreferenceManager().getNumWrite() > 1) {
                    showLayoutWriteComplete();
                } else {
                    showLayoutWrite();
                }

                buttonType.setText(getString(R.string.action_choose));
                textTitle.setText(getString(R.string.add_question_write));

            }

        });

        buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(view -> addQuestion());

        buttonCancel = findViewById(R.id.button_cancel);
        buttonCancel.setOnClickListener(view -> cancelEditing());

        buttonImage = findViewById(R.id.button_image);
        buttonImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!imagePath.equals("")) {

                    // リスト表示用のアラートダイアログ
                    AlertDialog.Builder listDlg = new AlertDialog.Builder(EditActivity.this, R.style.MyAlertDialogStyle);
                    listDlg.setItems(
                            getResources().getStringArray(R.array.action_image),
                            (dialog, which) -> {

                                switch (which) {
                                    case 0:
                                        openImage();
                                        break;
                                    case 1:
                                        imagePath = "";
                                        buttonImage.setImageResource(R.drawable.ic_photo_white);
                                        buttonImage.setBackgroundResource(R.drawable.button_blue);
                                        break;
                                }
                            });

                    // 表示
                    listDlg.show();

                } else {
                    openImage();
                }


            }

            void openImage() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).setType("image/*"), REQUEST_PICK_IMAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_SAF_PICK_IMAGE);
                }
            }
        });


        if (Build.VERSION.SDK_INT >= 21) {
            buttonAdd.setStateListAnimator(null);
            buttonCancel.setStateListAnimator(null);
            buttonType.setStateListAnimator(null);
            button_detail.setStateListAnimator(null);
        }

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        recyclerView.setAdapter(editAdapter);
    }
}
