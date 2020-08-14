package com.muradismayilov.englishdictionaryoffline.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.muradismayilov.englishdictionaryoffline.R;
import com.muradismayilov.englishdictionaryoffline.database.DatabaseHelper;
import com.muradismayilov.englishdictionaryoffline.model.History;
import com.muradismayilov.englishdictionaryoffline.tools.MyAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnSuggestionListener {

    // UI Components
    // Toolbar
    @BindView(R.id.activity_main_toolbar)
    MaterialToolbar activity_main_toolbar;
    // SearchView
    @BindView(R.id.layout_main_mainSV)
    SearchView layout_main_mainSV;
    // RecyclerView
    @BindView(R.id.layout_main_mainRV)
    RecyclerView layout_main_mainRV;
    // FrameLayout
    @BindView(R.id.layout_main_bannerAdFL)
    FrameLayout layout_main_bannerAdFL;

    // String
    @BindString(R.string.app_name)
    String app_name;
    @BindString(R.string.went_wrong)
    String went_wrong;
    @BindString(R.string.press_again)
    String press_again;
    @BindString(R.string.app_link)
    String app_link;
    @BindString(R.string.englishdictionaryoffline_main_banner)
    String englishdictionaryoffline_main_banner;

    // Variables
    // DatabaseHelper
    private DatabaseHelper databaseHelper;
    // AlertDialog
    private AlertDialog dialog_loading;
    // SimpleCursorAdapter
    private SimpleCursorAdapter simpleCursorAdapter;
    // List
    private List<History> historyList;
    // Boolean
    private boolean doubleBackToExitPressedOnce, isDatabaseOpen;
    // Adapter
    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initialFunctions();
    }

    private void initialFunctions() {
        declareVariables();
        setToolbar();
        setListeners();
        setSuggestion();
        showLoadingDialog();
        loadDatabase();
        setRecyclerView();
        fetchHistory();
        setAds();
    }

    private void setAds() {
        MobileAds.initialize(this);
        AdView adView = new AdView(this);
        adView.setAdUnitId(englishdictionaryoffline_main_banner);
        layout_main_bannerAdFL.addView(adView);

        AdRequest bannerAdRequest = new AdRequest.Builder().build();
        AdSize adSize = getAdSize();
        adView.setAdSize(adSize);
        adView.loadAd(bannerAdRequest);
    }

    private AdSize getAdSize() {
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void declareVariables() {
        // List
        historyList = new ArrayList<>();
        // Boolean
        doubleBackToExitPressedOnce = false;
        isDatabaseOpen = false;
    }

    private void setToolbar() {
        setSupportActionBar(activity_main_toolbar);
        MainActivity.this.setTitle(app_name);
    }

    private void setListeners() {
        layout_main_mainSV.setOnQueryTextListener(this);
    }

    private void showLoadingDialog() {
        dialog_loading = new AlertDialog.Builder(this).create();
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        dialog_loading.setView(view);
        dialog_loading.setCancelable(false);
        dialog_loading.setCanceledOnTouchOutside(false);
        dialog_loading.show();
    }

    private void loadDatabase() {
        databaseHelper = new DatabaseHelper(this);

        if (databaseHelper.checkDataBase()) {
            openDatabase();
            dialog_loading.dismiss();
        } else {
            DatabaseAsync task = new DatabaseAsync(MainActivity.this);
            task.execute();
        }
    }

    private void setRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        adapter = new MyAdapter(this, historyList);
        layout_main_mainRV.setLayoutManager(layoutManager);
        layout_main_mainRV.setAdapter(adapter);
    }

    private void fetchHistory() {
        historyList.clear();

        History history;

        if (isDatabaseOpen) {
            Cursor cursor = databaseHelper.getHistory();
            if (cursor.moveToFirst()) {
                do {
                    history = new History(cursor.getString(cursor.getColumnIndex("word")), cursor.getString(cursor.getColumnIndex("en_definition")));
                    historyList.add(history);
                }
                while (cursor.moveToNext());
            }
            adapter.notifyDataSetChanged();
        }
    }

    @OnClick(R.id.layout_main_mainSV)
    public void layout_main_mainSVClicked() {
        layout_main_mainSV.setIconified(false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
        Matcher m = p.matcher(query);

        if (m.matches()) {
            Cursor c = databaseHelper.getMeaning(query);

            if (c.getCount() == 0) {
                showAlertDialog();
            } else {
                layout_main_mainSV.clearFocus();
                layout_main_mainSV.setFocusable(false);

                Intent intent = new Intent(MainActivity.this, MeaningActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("en_word", query);
                intent.putExtras(bundle);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        } else {
            showAlertDialog();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        layout_main_mainSV.setIconifiedByDefault(false);

        Pattern p = Pattern.compile("[A-Za-z \\-.]{1,25}");
        Matcher m = p.matcher(newText);

        if (m.matches()) {
            Cursor cursorSuggestion = databaseHelper.getSuggestions(newText);
            simpleCursorAdapter.changeCursor(cursorSuggestion);
        }
        return false;
    }

    private void showAlertDialog() {
        AlertDialog dialog_alert = new AlertDialog.Builder(this).create();
        @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_alert, null);

        MaterialButton dialog_alert_okayBTN = view.findViewById(R.id.dialog_alert_okayBTN);
        dialog_alert_okayBTN.setOnClickListener(v -> {
            layout_main_mainSV.setQuery("", false);
            dialog_alert.dismiss();
        });

        dialog_alert.setView(view);
        dialog_alert.setCancelable(false);
        dialog_alert.setCanceledOnTouchOutside(false);
        dialog_alert.show();

    }

    @SuppressLint("StaticFieldLeak")
    class DatabaseAsync extends AsyncTask<Void, Void, Boolean> {

        private final Context context;

        DatabaseAsync(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            DatabaseHelper myDbHelper = new DatabaseHelper(context);

            try {
                myDbHelper.createDataBase();
            } catch (Exception e) {
                Toast.makeText(context, "" + went_wrong, Toast.LENGTH_SHORT).show();
            }
            myDbHelper.close();
            return null;
        }

        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            openDatabase();
            dialog_loading.dismiss();
        }
    }

    private void openDatabase() {
        try {
            databaseHelper.openDataBase();
            isDatabaseOpen = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setSuggestion() {
        final String[] from = new String[]{"en_word"};
        final int[] to = new int[]{R.id.suggestion_item_mainTV};

        simpleCursorAdapter = new SimpleCursorAdapter(MainActivity.this,
                R.layout.suggestion_item, null, from, to, 0) {
            @Override
            public void changeCursor(Cursor cursor) {
                super.swapCursor(cursor);
            }

        };
        layout_main_mainSV.setSuggestionsAdapter(simpleCursorAdapter);
        layout_main_mainSV.setOnSuggestionListener(this);
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        CursorAdapter ca = layout_main_mainSV.getSuggestionsAdapter();
        Cursor cursor = ca.getCursor();
        cursor.moveToPosition(position);
        String clicked_word = cursor.getString(cursor.getColumnIndex("en_word"));
        layout_main_mainSV.setQuery(clicked_word, false);

        layout_main_mainSV.clearFocus();
        layout_main_mainSV.setFocusable(false);

        Intent intent = new Intent(MainActivity.this, MeaningActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("en_word", clicked_word);
        intent.putExtras(bundle);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        return true;
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, press_again, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }

    private void deleteHistory() {
        if (isDatabaseOpen) {
            databaseHelper.deleteHistory();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_about:
                goToAbout();
                return true;
            case R.id.main_menu_rateUs:
                rateUs();
                return true;
            case R.id.main_menu_clearHistory:
                deleteHistory();
                fetchHistory();
                return true;
            case R.id.main_menu_exit:
                MainActivity.this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToAbout() {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void rateUs() {
        Uri open_uri = Uri.parse(app_link);
        Intent open_intent = new Intent(Intent.ACTION_VIEW, open_uri);
        startActivity(open_intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchHistory();
        layout_main_mainRV.requestFocus();
    }
}