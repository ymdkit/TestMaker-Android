package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.Locale;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest;

public class EditProActivity extends BaseActivity {

    Button buttonSave;
    EditText editTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pro);

        sendScreen("EditProActivity");

        Toolbar toolbar = findViewById(R.id.pro_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        buttonSave = findViewById(R.id.button_save);
        if (Build.VERSION.SDK_INT >= 21) {
            buttonSave.setStateListAnimator(null);
        }

        buttonSave.setOnClickListener(view -> {
            String text = editTest.getText().toString();

            AsyncLoadTest loader = new AsyncLoadTest(text.split("\n"), null, realmController, EditProActivity.this, getIntent().getLongExtra("testId", -1));
            loader.execute();
        });

        editTest = findViewById(R.id.editText);
        editTest.setText(realmController.getTest(getIntent().getLongExtra("testId", -1)).testToString(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_pro, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        int action_id = item.getItemId();

        if (action_id == android.R.id.home) {

            Intent i = new Intent(EditProActivity.this, EditActivity.class);

            i.putExtra("testId", getIntent().getLongExtra("testId", -1));

            startActivity(i);
            return true;
        } else if (action_id == R.id.nav_help) {

            Locale locale = Locale.getDefault();
            String lang = locale.getLanguage();
            if (lang.equals("ja")) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("https://banira0428.wixsite.com/testmaker/help")));
            } else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri
                        .parse("https://banira0428.wixsite.com/testmaker/help-en")));
            }
        }


        return super.onOptionsItemSelected(item);
    }

}