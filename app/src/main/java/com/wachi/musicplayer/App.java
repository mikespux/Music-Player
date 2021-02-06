package com.wachi.musicplayer;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.facebook.ads.AudienceNetworkAds;
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
