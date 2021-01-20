package com.wachi.musicplayer;

import android.app.Application;
import android.os.Build;

import com.facebook.ads.AudienceNetworkAds;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.wachi.musicplayer.appshortcuts.DynamicShortcutManager;

public class App extends Application {

    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this)
                    .primaryColorRes(R.color.primary_color)
                    .accentColorRes(R.color.accent_color)
                    .commit();
        }

        // Set up dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
        AudienceNetworkAds.initialize(this);
    }

    public static App getInstance() {
        return app;
    }
}
