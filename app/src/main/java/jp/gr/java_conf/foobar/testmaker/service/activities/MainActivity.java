package jp.gr.java_conf.foobar.testmaker.service.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Locale;

import jp.gr.java_conf.foobar.testmaker.service.IOUtil;
import jp.gr.java_conf.foobar.testmaker.service.R;
import jp.gr.java_conf.foobar.testmaker.service.models.AsyncLoadTest;
import jp.gr.java_conf.foobar.testmaker.service.models.CategoryEditor;
import jp.gr.java_conf.foobar.testmaker.service.models.Test;
import jp.gr.java_conf.foobar.testmaker.service.views.ColorChooser;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.FolderAdapter;
import jp.gr.java_conf.foobar.testmaker.service.views.adapters.MyScrambleAdapter;

public class MainActivity extends ShowTestsActivity {

    RecyclerView recyclerView;
    InputMethodManager inputMethodManager;
    ActionBarDrawerToggle drawerToggle;
    DrawerLayout drawerLayout;

    ColorChooser colorChooser;

    EditText editTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        sendScreen("MainActivity");

        LinearLayout container = findViewById(R.id.container);
        container.addView(createAd());

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        initNavigationView();

        initViews();

        initTestAdapter();

        FolderAdapter folderAdapter = new FolderAdapter(this, realmController);

        parentAdapter = new MyScrambleAdapter(this, realmController.getMixedList(),
                null, realmController,
                testAdapter,
                folderAdapter
        );

        folderAdapter.setOnClickListener(category -> {
            Intent i = new Intent(MainActivity.this, CategorizedActivity.class);
            i.putExtra("category", category);

            startActivityForResult(i,REQUEST_EDIT);
        });

        recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true); // アイテムは固定サイズ
        recyclerView.setAdapter(parentAdapter);

    }

    private void initViews() {

        colorChooser = findViewById(R.id.color_chooser);

        final ImageButton expand = findViewById(R.id.expand);
        expand.setOnClickListener(v -> {
            LinearLayout body = findViewById(R.id.body);

            if (body.getVisibility() != View.GONE) {
                body.setVisibility(View.GONE);
                expand.setImageResource(R.drawable.ic_expand_more_black);
            } else {
                body.setVisibility(View.VISIBLE);
                expand.setImageResource(R.drawable.ic_expand_less_black);
                editTitle.setFocusable(true);
                editTitle.requestFocus();
            }
        });

        editTitle = findViewById(R.id.set_title);
        editTitle.setOnFocusChangeListener((v, hasFocus) -> {
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

        final Button button_cate = findViewById(R.id.button_category);
        button_cate.setTag("");
        button_cate.setOnClickListener(view -> {
            inputMethodManager.hideSoftInputFromWindow(editTitle.getWindowToken(), 0);
            CategoryEditor categoryEditor = new CategoryEditor(MainActivity.this, button_cate, realmController, parentAdapter);
            categoryEditor.setCategory();
        });

        if (Build.VERSION.SDK_INT >= 21) {
            button_cate.setStateListAnimator(null);
        }

        button_cate.setOnLongClickListener(view -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
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

        final Button button_add = findViewById(R.id.button_add);

        if (Build.VERSION.SDK_INT >= 21) {
            button_add.setStateListAnimator(null);
        }

        button_add.setOnClickListener(v -> {

            if (String.valueOf(editTitle.getText()).equals("")) {

                Toast.makeText(MainActivity.this, getString(R.string.message_wrong), Toast.LENGTH_LONG).show();

            } else {

                realmController.addTest(editTitle.getText().toString(), colorChooser.getColorId(), button_cate.getTag().toString());

                Toast.makeText(MainActivity.this, getString(R.string.message_add), Toast.LENGTH_LONG).show();

                parentAdapter.notifyDataSetChanged();

                editTitle.setText("");
                button_cate.setTag("");
                button_cate.setText(getString(R.string.category));

                button_cate.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_blue));

                LinearLayout body = findViewById(R.id.body);
                body.setVisibility(View.GONE);
                expand.setImageResource(R.drawable.ic_expand_more_black);

                sendEvent("createTest");

            }

        });

    }

    private void initNavigationView() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {

            switch (menuItem.getItemId()) {
                case R.id.nav_help: //editProActivityにも同様の記述

                    sendEvent("help");

                    Locale locale = Locale.getDefault();
                    String lang = locale.getLanguage();
                    if (lang.equals("ja")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse("https://banira0428.wixsite.com/testmaker/help")));
                    } else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri
                                .parse("https://banira0428.wixsite.com/testmaker/help-en")));
                    }


                    break;
                case R.id.nav_review:

                    sendEvent("review");

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://play.google.com/store/apps/details?id=jp.gr.java_conf.foobar.testmaker.service&amp;hl=ja")));
                    break;

                case R.id.nav_others:

                    sendEvent("others");

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("http://play.google.com/store/apps/developer?id=banira")));
                    break;

                case R.id.nav_import:

                    sendEvent("import");

                    if (Build.VERSION.SDK_INT <= 18) {
                        //APIレベル18以前の機種の場合の処理
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("text/*");
                        startActivityForResult(intent, 12346);
                    } else if (Build.VERSION.SDK_INT >= 19) {
                        //APIレベル19以降の機種の場合の処理
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("text/*");
                        startActivityForResult(intent, 12345);

                    }
                    break;

                case R.id.nav_license:

                    Intent licenseIntent = new Intent(MainActivity.this, WebViewActivity.class);
                    licenseIntent.putExtra("url", "file:///android_asset/licenses.html");
                    startActivity(licenseIntent);

                    break;
                case R.id.nav_paste:

                    sendEvent("paste");

                    final View dialogLayout = getLayoutInflater().inflate(R.layout.dialog_paste, findViewById(R.id.layout_dialog_paste));

                    AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
                    builder.setView(dialogLayout);
                    builder.setTitle(getString(R.string.action_paste));
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setNegativeButton(android.R.string.cancel, null);

                    final AlertDialog dialog = builder.show();

                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if (positiveButton != null) {
                        positiveButton.setVisibility(View.GONE);
                    }

                    Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);

                    if (negativeButton != null) {
                        negativeButton.setVisibility(View.GONE);
                    }

                    final EditText edit_paste = dialogLayout.findViewById(R.id.edit_paste);
                    final Button button_import = dialogLayout.findViewById(R.id.button_paste);

                    if (Build.VERSION.SDK_INT >= 21) {
                        button_import.setStateListAnimator(null);

                    }

                    button_import.setOnClickListener(view -> {

                        String paste = edit_paste.getText().toString();

                        AsyncLoadTest loader = new AsyncLoadTest(paste.split("\n"), parentAdapter, realmController, MainActivity.this);
                        loader.execute();

                        dialog.dismiss();
                    });

                    break;

                case R.id.nav_cloud:

                    sendEvent("cloud");

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri
                            .parse("https://drive.google.com/drive/folders/0BxTVsyahB5u9TGhxTUtkNGwwRlE")));
                    break;
            }
            return false;
        });

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.add,
                R.string.add);

        drawerLayout.addDrawerListener(drawerToggle);

    }

    void startAnswer(Test test, EditText editLimit, boolean rand) {
        boolean incorrect = false;

        for (int k = 0; k < test.getQuestions().size(); k++) {
            if (!test.getQuestions().get(k).getCorrect()) {
                incorrect = true;
            }
        }

        if (!incorrect && sharedPreferenceManager.isRefine()) {
            Toast.makeText(MainActivity.this, getString(R.string.message_null_wrongs), Toast.LENGTH_SHORT).show();
        } else if (editLimit.getText().toString().equals("")) {
            Toast.makeText(MainActivity.this, getString(R.string.message_null_number), Toast.LENGTH_SHORT).show();
        } else {

            Intent i = new Intent(MainActivity.this, PlayActivity.class);
            i.putExtra("testId", test.getId());

            if (rand) {
                i.putExtra("random", 1);

            }

            realmController.updateLimit(test, Integer.parseInt(editLimit.getText().toString()));

            realmController.updateHistory(test);

            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        super.onBackPressed();
    }

    protected void onPause() {
        inputMethodManager.hideSoftInputFromWindow(editTitle.getWindowToken(), 0);

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_CANCELED){
            parentAdapter.notifyDataSetChanged();
        }

        super.onActivityResult(requestCode,resultCode,data);

        if (resultCode != RESULT_OK)
            return;

        if (requestCode == 12345) {

            Uri uri = data.getData();
            launchEditorActivity(uri);
        } else if (requestCode == 12346) {

            Uri uri = data.getData();
            launchEditorActivity(uri);

        }

    }


    private void launchEditorActivity(Uri uri) {

        if (uri == null)
            return;

        InputStream inputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "SJIS"));

            String line;
            ArrayList<String> strings = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                byte[] b = line.getBytes();
                line = new String(b, "UTF-8");
                strings.add(line);
            }

            reader.close();


            inputStream = getContentResolver().openInputStream(uri);
            BufferedReader readUTF = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));

            while ((line = readUTF.readLine()) != null) {
                byte[] b = line.getBytes();
                line = new String(b, "UTF-8");
                strings.add(line);
            }

            readUTF.close();

            inputStream.close();

            AsyncLoadTest loader = new AsyncLoadTest(strings.toArray(new String[0]), parentAdapter, realmController, MainActivity.this);
            loader.execute();

        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            IOUtil.forceClose(inputStream);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

}
