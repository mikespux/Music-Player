package com.wachi.musicplayer.ui.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEColorPreference;
import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEPreferenceFragmentCompat;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.wachi.musicplayer.R;
import com.wachi.musicplayer.appshortcuts.DynamicShortcutManager;
import com.wachi.musicplayer.misc.NonProAllowedColors;
import com.wachi.musicplayer.preferences.BlacklistPreference;
import com.wachi.musicplayer.preferences.BlacklistPreferenceDialog;
import com.wachi.musicplayer.preferences.LibraryPreference;
import com.wachi.musicplayer.preferences.LibraryPreferenceDialog;
import com.wachi.musicplayer.preferences.NowPlayingScreenPreference;
import com.wachi.musicplayer.preferences.NowPlayingScreenPreferenceDialog;
import com.wachi.musicplayer.ui.activities.base.AbsBaseActivity;
import com.wachi.musicplayer.util.PreferenceUtil;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingsActivity extends AbsBaseActivity implements ColorChooserDialog.ColorCallback {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private AdView adView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitleTextAppearance(this, R.style.ProductSansTextAppearace);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new SettingsFragment()).commit();
        } else {
            SettingsFragment frag = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (frag != null) frag.invalidateSettings();
        }
      //  adView = findViewById(R.id.ad_view);
        if (getResources().getString(R.string.ADS_VISIBILITY).equals("YES")) {
            fbAdview();
        }else{
            //adView.setVisibility(View.GONE);
        }
    }
    public  void fbAdview(){

       // adView = new com.facebook.ads.AdView(this, "IMG_16_9_APP_INSTALL#YOUR_PLACEMENT_ID", AdSize.BANNER_HEIGHT_50);
        adView = new AdView(this, getResources().getString(R.string.fbbanner_unit_id), AdSize.BANNER_HEIGHT_50);
        // Find the Ad Container
        LinearLayout adContainer = findViewById(R.id.banner_container);

        // Add the ad view to your activity layout
        adContainer.addView(adView);

        // Request an ad
        //  adView.loadAd();

        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
               // Toast.makeText(SettingsActivity.this,"Error: " + adError.getErrorMessage(),Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };

        // Request an ad
        adView.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build());
    }
//    public void adview(){
//        // Initialize the Mobile Ads SDK.
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {}
//        });
//
//        // Set your test devices. Check your logcat output for the hashed device ID to
//        // get test ads on a physical device. e.g.
//        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
//        // to get test ads on this device."
//        MobileAds.setRequestConfiguration(
//                new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
//                        .build());
//
//        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
//        // values/strings.xml.
//
//
//        // Create an ad request.
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        // Start loading the ad in the background.
//        adView.loadAd(adRequest);
//    }
    /** Called when leaving the activity */
    @Override
    public void onPause() {
//        if (adView != null) {
//            adView.pause();
//        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
//        if (adView != null) {
//            adView.resume();
//        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        switch (dialog.getTitle()) {
            case R.string.primary_color:
                    Arrays.sort(NonProAllowedColors.PRIMARY_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.PRIMARY_COLORS, selectedColor) < 0) {
                }
                ThemeStore.editTheme(this)
                        .primaryColor(selectedColor)
                        .commit();
                break;
            case R.string.accent_color:
                    Arrays.sort(NonProAllowedColors.ACCENT_COLORS);
                    if (Arrays.binarySearch(NonProAllowedColors.ACCENT_COLORS, selectedColor) < 0) {
                    }
                ThemeStore.editTheme(this)
                        .accentColor(selectedColor)
                        .commit();
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).updateDynamicShortcuts();
        }
        recreate();
    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog dialog) {
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends ATEPreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

        private static void setSummary(@NonNull Preference preference) {
            setSummary(preference, PreferenceManager
                    .getDefaultSharedPreferences(preference.getContext())
                    .getString(preference.getKey(), ""));
        }

        private static void setSummary(Preference preference, @NonNull Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            } else {
                preference.setSummary(stringValue);
            }
        }

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            addPreferencesFromResource(R.xml.pref_library);
            addPreferencesFromResource(R.xml.pref_colors);
            addPreferencesFromResource(R.xml.pref_notification);
            addPreferencesFromResource(R.xml.pref_now_playing_screen);
            addPreferencesFromResource(R.xml.pref_images);
            addPreferencesFromResource(R.xml.pref_lockscreen);
            addPreferencesFromResource(R.xml.pref_audio);
            addPreferencesFromResource(R.xml.pref_playlists);
            addPreferencesFromResource(R.xml.pref_blacklist);
        }

        @Nullable
        @Override
        public DialogFragment onCreatePreferenceDialog(Preference preference) {
            if (preference instanceof NowPlayingScreenPreference) {
                return NowPlayingScreenPreferenceDialog.newInstance();
            } else if (preference instanceof BlacklistPreference) {
                return BlacklistPreferenceDialog.newInstance();
            } else if (preference instanceof LibraryPreference) {
                return LibraryPreferenceDialog.newInstance();
            }
            return super.onCreatePreferenceDialog(preference);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getListView().setPadding(0, 0, 0, 0);
            invalidateSettings();
            PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
        }

        private void invalidateSettings() {
            final Preference generalTheme = findPreference("general_theme");
            setSummary(generalTheme);
            generalTheme.setOnPreferenceChangeListener((preference, o) -> {
                String themeName = (String) o;
                if (themeName.equals("black")) {
                }

                setSummary(generalTheme, o);

                ThemeStore.markChanged(getActivity());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                    // Set the new theme so that updateAppShortcuts can pull it
                    getActivity().setTheme(PreferenceUtil.getThemeResFromPrefValue(themeName));
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();
                }

                getActivity().recreate();
                return true;
            });

            final Preference autoDownloadImagesPolicy = findPreference("auto_download_images_policy");
            setSummary(autoDownloadImagesPolicy);
            autoDownloadImagesPolicy.setOnPreferenceChangeListener((preference, o) -> {
                setSummary(autoDownloadImagesPolicy, o);
                return true;
            });

            final ATEColorPreference primaryColorPref = (ATEColorPreference) findPreference("primary_color");
             //final int primaryColor = ThemeStore.primaryColor(getActivity());
             final int primaryColor = Color.parseColor("#000000");
            primaryColorPref.setColor(primaryColor, ColorUtil.darkenColor(primaryColor));
            primaryColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(getActivity(), R.string.primary_color)
                        .accentMode(false)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(primaryColor)
                        .show(getActivity());
                return true;
            });

            final ATEColorPreference accentColorPref = (ATEColorPreference) findPreference("accent_color");
          //  final int accentColor = ThemeStore.accentColor(getActivity());
            final int accentColor = Color.parseColor("#FF6500");
            accentColorPref.setColor(accentColor, ColorUtil.darkenColor(accentColor));
            accentColorPref.setOnPreferenceClickListener(preference -> {
                new ColorChooserDialog.Builder(getActivity(), R.string.accent_color)
                        .accentMode(true)
                        .allowUserColorInput(true)
                        .allowUserColorInputAlpha(false)
                        .preselect(accentColor)
                        .show(getActivity());
                return true;
            });

            TwoStatePreference colorNavBar = (TwoStatePreference) findPreference("should_color_navigation_bar");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                colorNavBar.setVisible(false);
            } else {
                colorNavBar.setChecked(ThemeStore.coloredNavigationBar(getActivity()));
                colorNavBar.setOnPreferenceChangeListener((preference, newValue) -> {
                    ThemeStore.editTheme(getActivity())
                            .coloredNavigationBar((Boolean) newValue)
                            .commit();
                    getActivity().recreate();
                    return true;
                });
            }

            final TwoStatePreference classicNotification = (TwoStatePreference) findPreference("classic_notification");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                classicNotification.setVisible(false);
            } else {
                classicNotification.setChecked(PreferenceUtil.getInstance(getActivity()).classicNotification());
                classicNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setClassicNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference coloredNotification = (TwoStatePreference) findPreference("colored_notification");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                coloredNotification.setEnabled(PreferenceUtil.getInstance(getActivity()).classicNotification());
            } else {
                coloredNotification.setChecked(PreferenceUtil.getInstance(getActivity()).coloredNotification());
                coloredNotification.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredNotification((Boolean) newValue);
                    return true;
                });
            }

            final TwoStatePreference colorAppShortcuts = (TwoStatePreference) findPreference("should_color_app_shortcuts");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
                colorAppShortcuts.setVisible(false);
            } else {
                colorAppShortcuts.setChecked(PreferenceUtil.getInstance(getActivity()).coloredAppShortcuts());
                colorAppShortcuts.setOnPreferenceChangeListener((preference, newValue) -> {
                    // Save preference
                    PreferenceUtil.getInstance(getActivity()).setColoredAppShortcuts((Boolean) newValue);

                    // Update app shortcuts
                    new DynamicShortcutManager(getActivity()).updateDynamicShortcuts();

                    return true;
                });
            }

            updateNowPlayingScreenSummary();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PreferenceUtil.NOW_PLAYING_SCREEN_ID:
                    updateNowPlayingScreenSummary();
                    break;
                case PreferenceUtil.CLASSIC_NOTIFICATION:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        findPreference("colored_notification").setEnabled(sharedPreferences.getBoolean(key, false));
                    }
                    break;
            }
        }

        private void updateNowPlayingScreenSummary() {
            findPreference("now_playing_screen_id").setSummary(PreferenceUtil.getInstance(getActivity()).getNowPlayingScreen().titleRes);
        }
    }
}
