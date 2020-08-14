package com.muradismayilov.englishdictionaryoffline.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MenuItem;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.muradismayilov.englishdictionaryoffline.R;
import com.muradismayilov.englishdictionaryoffline.database.DatabaseHelper;
import com.muradismayilov.englishdictionaryoffline.fragments.AntonymsFragment;
import com.muradismayilov.englishdictionaryoffline.fragments.DefinitionFragment;
import com.muradismayilov.englishdictionaryoffline.fragments.ExampleFragment;
import com.muradismayilov.englishdictionaryoffline.fragments.SynonymsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MeaningActivity extends AppCompatActivity {

    // UI Components
    // MaterialToolbar
    @BindView(R.id.activity_meaning_toolbar)
    MaterialToolbar activity_meaning_toolbar;
    // ViewPager
    @BindView(R.id.activity_meaning_viewpager)
    ViewPager activity_meaning_viewpager;
    // TabLayout
    @BindView(R.id.activity_meaning_tablayout)
    TabLayout activity_meaning_tablayout;
    // FrameLayout
    @BindView(R.id.activity_meaning_bannerAdFL)
    FrameLayout activity_meaning_bannerAdFL;

    // String
    @BindString(R.string.not_available)
    String not_available;
    @BindString(R.string.englishdictionaryoffline_meaning_banner)
    String englishdictionaryoffline_meaning_banner;

    // Variables
    // String
    private String enWord;
    // DatabaseHelper
    private DatabaseHelper databaseHelper;
    // Public String
    public String definition, example, synonyms, antonyms;
    // TextToSpeech
    private TextToSpeech textToSpeech;
    // Boolean
    private boolean startedFromShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI() {
        setContentView(R.layout.activity_meaning);
        ButterKnife.bind(this);
        initialFunctions();
    }

    private void initialFunctions() {
        declareVariables();
        getExtra();
        openDatabase();
        getAll();
        setToolbar();
        setViewPager();
        setTabLayout();
        setAds();
    }

    private void setAds() {
        MobileAds.initialize(this);
        AdView adView = new AdView(this);
        adView.setAdUnitId(englishdictionaryoffline_meaning_banner);
        activity_meaning_bannerAdFL.addView(adView);

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
        // String
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        enWord = bundle.getString("en_word");
        // Boolean
        startedFromShare = false;
    }

    private void getExtra() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                startedFromShare = true;

                if (sharedText != null) {
                    Pattern p = Pattern.compile("[A-Za-z ]{1,25}");
                    Matcher m = p.matcher(sharedText);

                    if (m.matches()) {
                        enWord = sharedText;
                    } else {
                        enWord = not_available;
                    }
                }
            }
        }
    }

    private void openDatabase() {
        databaseHelper = new DatabaseHelper(this);
        try {
            databaseHelper.openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getAll() {
        Cursor cursor = databaseHelper.getMeaning(enWord);

        if (cursor.moveToFirst()) {

            definition = cursor.getString(cursor.getColumnIndex("en_definition"));
            example = cursor.getString(cursor.getColumnIndex("example"));
            synonyms = cursor.getString(cursor.getColumnIndex("synonyms"));
            antonyms = cursor.getString(cursor.getColumnIndex("antonyms"));
            databaseHelper.insertHistory(enWord);
        } else {
            enWord = "Not Available";
        }
    }

    @OnClick(R.id.activity_meaning_soundIB)
    public void activity_meaning_soundIBClicked() {
        textToSpeech = new TextToSpeech(MeaningActivity.this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.getDefault());
                textToSpeech.speak(enWord, TextToSpeech.QUEUE_FLUSH, null);
            }
        });
    }

    private void setToolbar() {
        setSupportActionBar(activity_meaning_toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(enWord);
        }
        activity_meaning_toolbar.setNavigationIcon(R.drawable.ic_back);
    }

    private void setViewPager() {
        if (activity_meaning_viewpager != null) {
            setupViewPager(activity_meaning_viewpager);
        }
    }

    private void setTabLayout() {
        activity_meaning_tablayout.setupWithViewPager(activity_meaning_viewpager);
        activity_meaning_tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                activity_meaning_viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(new DefinitionFragment(), "Definition");
        adapter.addFrag(new SynonymsFragment(), "Synonyms");
        adapter.addFrag(new AntonymsFragment(), "Antonyms");
        adapter.addFrag(new ExampleFragment(), "Example");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (startedFromShare) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                onBackPressed();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
