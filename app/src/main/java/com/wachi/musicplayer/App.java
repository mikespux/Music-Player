package com.wachi.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.wachi.musicplayer.appshortcuts.DynamicShortcutManager;

public class App extends Application {

    private static App app;
    SharedPreferences prefs;
    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        prefs = PreferenceManager.getDefaultSharedPreferences(app);
        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.primary_color)
                    .accentColorRes(R.color.accent_color)
                    .commit();
            setThemePrefs("dark");
        }

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
        AudienceNetworkAds.initialize(this);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, initializationStatus -> {
        });
        AdSettings.addTestDevice("22a02ff6-199e-4732-9280-3c7579d78a7b");
        AdSettings.addTestDevice("00acc125-7dc6-427a-8781-1d5af92258e7");
    }
    public void setThemePrefs(final String theme) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString("general_theme", theme);
        editor.apply();
    }
    public static App getInstance() {
        return app;
    }
}
