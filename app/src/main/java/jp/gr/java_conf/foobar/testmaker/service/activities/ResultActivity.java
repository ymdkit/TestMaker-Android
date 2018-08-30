package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.Quest;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.ResultAdapter;

public class ResultActivity extends BaseActivity{
    ResultAdapter mAdapter;
    long testId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        sendScreen("ResultActivity");

        testId = getIntent().getLongExtra("testId", -1);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

    }

    @Override
    protected void onResume(){
        super.onResume();

        final ArrayList<Quest> questions = realmController.getQuestionsSolved(testId);

        mAdapter = new ResultAdapter(this, realmController, testId);
        RecyclerView mRecyclerView = findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        mRecyclerView.setAdapter(mAdapter);

        final TextView result = findViewById(R.id.result);
        int count = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrect()) {
                count++;
            }
        }

        result.setText(getString(R.string.message_ratio,count,questions.size()));

        ImageButton top = findViewById(R.id.top);
        top.setOnClickListener(v -> startActivity(new Intent(ResultActivity.this, MainActivity.class)));

        ImageButton retry = findViewById(R.id.retry);
        retry.setOnClickListener(v -> new AlertDialog.Builder(ResultActivity.this, R.style.MyAlertDialogStyle)
                .setTitle(getString(R.string.retry))
                .setItems(getResources().getStringArray(R.array.action_reload), (dialog, which) -> {
                    Intent i = new Intent(ResultActivity.this, PlayActivity.class);
                    i.putExtra("testId", testId);

                    if (getIntent().hasExtra("random")) {
                        i.putExtra("random", getIntent().getIntExtra("random", -1));
                    }

                    i.putExtra("redo",1);

                    switch (which) {
                        case 0:

                            startActivity(i);
                            break;
                        case 1:

                            boolean incorrect=false;

                            for(int k=0;k<questions.size();k++){
                                if(!questions.get(k).getCorrect()){
                                    incorrect=true;
                                }
                            }

                            if (incorrect) {

                                sharedPreferenceManager.setRefine(true);

                                startActivity(i);

                            } else {

                                Toast.makeText(getApplicationContext(), getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show();

                            }

                            break;
                    }

                }).show());
    }

    @Override
    public void onBackPressed() {

        startActivity(new Intent(ResultActivity.this, MainActivity.class));

        super.onBackPressed();
    }

}
