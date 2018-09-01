package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Test;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.TestAdapter;

public class ShowTestsActivity extends BaseActivity {

    TestAdapter testAdapter;

    ScrambleAdapter parentAdapter;

    int REQUEST_EDIT = 11111;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected void initTestAdapter() {

        testAdapter = new TestAdapter(this);

        testAdapter.setOnClickListener(new TestAdapter.OnClickListener() {


            @Override
            public void onClickPlayTest(long id) {

                Test test = realmController.getTest(id);

                if (test.getQuestions().size() == 0) {

                    Toast.makeText(ShowTestsActivity.this, getString(R.string.message_null_questions), Toast.LENGTH_SHORT).show();

                } else {

                    initDialogPlayStart(test);

                }

            }

            @Override
            public void onClickEditTest(long id) {

                Intent i = new Intent(ShowTestsActivity.this, EditActivity.class);

                i.putExtra("testId", id);

                startActivityForResult(i, REQUEST_EDIT);
            }

            @Override
            public void onClickDeleteTest(long id) {

                Test test = realmController.getTest(id);

                AlertDialog.Builder builder = new AlertDialog.Builder(ShowTestsActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle(getString(R.string.delete_exam));
                builder.setMessage(getString(R.string.message_delete_exam, test.getTitle()));
                builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {

                    realmController.deleteTest(test);

                    if (parentAdapter != null) {

                        parentAdapter.notifyDataSetChanged();

                    }

                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();

            }
        });

    }

    private void initDialogPlayStart(Test test) {

        final View dialogLayout = LayoutInflater.from(ShowTestsActivity.this).inflate(R.layout.dialog_start, findViewById(R.id.layout_dialog_start));

        final EditText editLimit = dialogLayout.findViewById(R.id.set_limit);
        editLimit.setText(String.valueOf(test.getLimit()));

        final CheckBox checkReverse = dialogLayout.findViewById(R.id.check_reverse);
        checkReverse.setChecked(sharedPreferenceManager.isReverse());
        checkReverse.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferenceManager.setReverse(isChecked));

        final CheckBox checkManual = dialogLayout.findViewById(R.id.check_manual);
        checkManual.setChecked(sharedPreferenceManager.isManual());
        checkManual.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferenceManager.setManual(isChecked));

        final CheckBox checkAudio = dialogLayout.findViewById(R.id.check_audio);
        checkAudio.setChecked(sharedPreferenceManager.isAudio());
        checkAudio.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferenceManager.setAudio(isChecked));

        final CheckBox checkRefine = dialogLayout.findViewById(R.id.check_refine);
        checkRefine.setChecked(sharedPreferenceManager.isRefine());
        checkRefine.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferenceManager.setRefine(isChecked));

        final Button actionNormal = dialogLayout.findViewById(R.id.action_normal);
        actionNormal.setOnClickListener(v -> startAnswer(test, editLimit, false));

        final Button actionRandom = dialogLayout.findViewById(R.id.action_random);
        actionRandom.setOnClickListener(view -> startAnswer(test, editLimit, true));

        if (Build.VERSION.SDK_INT >= 21) {
            actionNormal.setStateListAnimator(null);
            actionRandom.setStateListAnimator(null);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ShowTestsActivity.this, R.style.MyAlertDialogStyle);
        builder.setView(dialogLayout);
        builder.setTitle(getString(R.string.way));
        builder.setPositiveButton(android.R.string.ok, null);
        builder.setNegativeButton(android.R.string.cancel, null);
        AlertDialog dialog = builder.show();

        hideDefaultButtonsFromDialog(dialog);

    }

    private void hideDefaultButtonsFromDialog(AlertDialog dialog) {

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        if (positiveButton != null) {
            positiveButton.setVisibility(View.GONE);
        }

        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

        if (negativeButton != null) {
            negativeButton.setVisibility(View.GONE);
        }

    }

    void startAnswer(Test test, EditText editLimit, boolean rand) {

        boolean incorrect = false;

        for (int k = 0; k < test.getQuestions().size(); k++) {

            if (!test.getQuestions().get(k).getCorrect()) {
                incorrect = true;
            }
        }

        if (!incorrect && sharedPreferenceManager.isRefine()) {

            Toast.makeText(ShowTestsActivity.this, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show();

        } else if (editLimit.getText().toString().equals("")) {

            Toast.makeText(ShowTestsActivity.this, getString(R.string.message_null_number), Toast.LENGTH_SHORT).show();

        } else {

            Intent i = new Intent(ShowTestsActivity.this, PlayActivity.class);
            i.putExtra("testId", test.getId());

            if (rand) i.putExtra("random", 1);

            realmController.updateLimit(test, Integer.parseInt(editLimit.getText().toString()));

            realmController.updateHistory(test);

            startActivityForResult(i,REQUEST_EDIT);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_CANCELED) {

            if (parentAdapter != null) {

                parentAdapter.notifyDataSetChanged();

            }

        }

        if(requestCode == REQUEST_EDIT){

            if (parentAdapter != null) {
                parentAdapter.notifyDataSetChanged();
            }

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == R.id.action_compare) {

            new AlertDialog.Builder(this, R.style.MyAlertDialogStyle)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setTitle(getString(R.string.sort))
                    .setItems(getResources().getStringArray(R.array.sort_exam), (dialog, which) -> {

                        sharedPreferenceManager.setSort(which);

                        if (parentAdapter != null) {

                            parentAdapter.notifyDataSetChanged();

                        }

                    }).show();

        }

        return super.onOptionsItemSelected(item);
    }

}
