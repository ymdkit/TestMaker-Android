package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.LinearLayout;

import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.CategorizedAdapter;

public class CategorizedActivity extends ShowTestsActivity {

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorized);

        sendScreen("CategorizedActivity");

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        initToolBar();

        initTestAdapter();

        parentAdapter = new CategorizedAdapter(this, realmController.getMixedList(),
                null, realmController, getIntent().getStringExtra("category"),
                testAdapter

        );

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        recyclerView.setAdapter(parentAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (item.getItemId() == android.R.id.home) {

            finish();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
