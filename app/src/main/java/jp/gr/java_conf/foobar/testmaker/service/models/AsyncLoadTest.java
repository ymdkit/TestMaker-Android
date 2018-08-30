package jp.gr.java_conf.foobar.testmaker.service.models;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import net.cattaka.android.adaptertoolbox.adapter.ScrambleAdapter;

import java.util.ArrayList;

import jp.gr.java_conf.foobar.testmaker.service.R;
/**
 * Created by keita on 2017/05/03.
 */

public class AsyncLoadTest extends AsyncTask<String, Integer, StructTest> {

    private final RealmController realmController;
    private String[] backups;
    private Context context;
    private ArrayList<Integer> errors;

    private ScrambleAdapter<Object> adapter;

    private AlertDialog alert;

    private long testId;

    public AsyncLoadTest(String[] text, ScrambleAdapter<Object> recycleradapter, RealmController realm, Context context) {

        adapter = recycleradapter;
        backups = text;
        realmController = realm;
        this.context = context;
        errors = new ArrayList<>();
        this.testId = -1;
    }

    public AsyncLoadTest(String[] text, ScrambleAdapter<Object> recycleradapter, RealmController realm, Context context, long testId) {

        adapter = recycleradapter;
        backups = text;
        realmController = realm;
        this.context = context;
        errors = new ArrayList<>();

        this.testId = testId;
    }


    // doInBackgroundの事前準備処理（UIスレッド）
    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        alert = new AlertDialog.Builder(context, R.style.MyAlertDialogStyle)
                .setTitle(context.getString(R.string.loading))
                .setView(R.layout.dialog_progress)
                .show();
    }

    @Override
    protected StructTest doInBackground(String... strs) {

        StructTest q = new StructTest(context.getString(R.string.unknown));

        int resultNumber = 0;

        for (int i = 0; i < backups.length; i++) {

            try {

                String[] backup = backups[i].replaceAll("<br>", "\n").split(",");

                for (int k = 0; k < backup.length; k++) {
                    Log.d(String.valueOf(k), backup[k]);
                }

                if (backup.length > 2) {

                    if (backup[0].equals(context.getString(R.string.load_short_answers))) {
                        q.setStructQuestion(backup[1], backup[2], resultNumber);
                        resultNumber += 1;
                    } else if (backup[0].equals(context.getString(R.string.load_multiple_answers))) {
                        String[] answers;
                        if (backup.length - 2 < 5) {
                            answers = new String[backup.length - 2];

                        } else {
                            break;
                        }

                        for (int k = 2; k < backup.length; k++) {
                            answers[k - 2] = backup[k];
                        }

                        q.setStructQuestion(backup[1], answers, resultNumber);
                        resultNumber += 1;
                    } else if (backup[0].equals(context.getString(R.string.load_selection_problems))) {
                        String[] others;

                        if (backup.length - 3 < 6) {

                            others = new String[backup.length - 3];

                        } else {
                            break;
                        }

                        for (int k = 3; k < backup.length; k++) {
                            others[k - 3] = backup[k];
                        }

                        q.setStructQuestion(backup[1], backup[2], others, resultNumber);
                        resultNumber += 1;
                    } else if (backup[0].equals(context.getString(R.string.load_selection_auto_problems))) {
                        String[] other;

                        int otherNum = Integer.parseInt(backup[3].substring(0, 1));

                        if (otherNum < 6) {
                            other = new String[otherNum];
                        } else {
                            break;
                        }

                        for (int k = 0; k < other.length; k++) {
                            other[k] = "自動生成";
                        }

                        q.setStructQuestion(backup[1], backup[2], other, resultNumber);
                        q.problems.get(resultNumber).auto = true;

                        resultNumber += 1;
                    }

                } else if (backup.length == 2) {

                    if (backup[0].equals(context.getString(R.string.load_explanation))) {
                        if (resultNumber > 0) {
                            q.problems.get(resultNumber - 1).setExplanation(backup[1]);
                        }
                    } else if (backup[0].equals(context.getString(R.string.load_title))) {
                        q.setTitle(backup[1]);
                    } else if (backup[0].equals(context.getString(R.string.load_category))) {
                        q.setCategory(backup[1]);
                    } else if (backup[0].equals(context.getString(R.string.load_color))) {
                        q.setColor(Integer.parseInt(backup[1]));
                    }
                }

            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                errors.add(i + 1);
            }
        }

        return q;
    }

    // doInBackgroundの事後処理(UIスレッド)
    protected void onPostExecute(StructTest result) {

        StringBuilder error = new StringBuilder();

        for (int i = 0; i < errors.size(); i++) {
            error.append(String.valueOf(errors.get(i))).append(" ");
        }

        if (!error.toString().equals("")) {
            Toast.makeText(context, context.getString(R.string.message_wrong_load, error.toString()), Toast.LENGTH_LONG).show();
        } else {

            if (testId != -1) {
                Toast.makeText(context, context.getString(R.string.message_success_update), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, context.getString(R.string.message_success_load, result.getTitle()), Toast.LENGTH_LONG).show();
            }
        }


        realmController.convert(result, testId);

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        alert.dismiss();

    }
}
