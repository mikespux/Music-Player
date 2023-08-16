package com.wachi.musicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.wachi.musicplayer.R;
import com.wachi.musicplayer.dialogs.ScanMediaFolderChooserDialog;
import com.wachi.musicplayer.glide.SongGlideRequest;
import com.wachi.musicplayer.helper.MusicPlayerRemote;
import com.wachi.musicplayer.helper.SearchQueryHelper;
import com.wachi.musicplayer.loader.AlbumLoader;
import com.wachi.musicplayer.loader.ArtistLoader;
import com.wachi.musicplayer.loader.PlaylistSongLoader;
import com.wachi.musicplayer.model.Song;
import com.wachi.musicplayer.removeads.InAppBillingActivity;
import com.wachi.musicplayer.service.MusicService;
import com.wachi.musicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.wachi.musicplayer.ui.activities.intro.AppIntroActivity;
import com.wachi.musicplayer.ui.fragments.mainactivity.folders.FoldersFragment;
import com.wachi.musicplayer.ui.fragments.mainactivity.library.LibraryFragment;
import com.wachi.musicplayer.util.MusicUtil;
import com.wachi.musicplayer.util.NavigationUtil;
import com.wachi.musicplayer.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import guy4444.smartrate.SmartRate;

public class MainActivity extends AbsSlidingMusicPanelActivity {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final int APP_INTRO_REQUEST = 100;

    private static final int LIBRARY = 0;
    private static final int FOLDERS = 1;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    @Nullable
    private View navigationDrawerHeader;

    private boolean blockRequestPermissions;

    AdRequest adRequest;
    private AdView adView;
    View contentView;

    private InterstitialAd interstitialAd;
    String applovin_native_unit_id = "";
    String applovin_banner_unit_id = "", applovin_interstitial_unit_id = "";


    private final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings;
    // cache expiration in seconds
    long cacheExpiration = 3600;

    String app_unit_id = "", banner_ad_unit_id = "", native_ad_unit_id = "", interstitial_ad_unit_id = "";

    String app_version = "", app_info = "";
    LinearLayout max_adplaceholder;
    FrameLayout nativeAdContainer;
    // Find the Ad Container
    LinearLayout adContainer;

    private NativeAd nativeAd;
    LinearLayout admob_adplaceholder;
    private MaxInterstitialAd minterstitialAd;
    private int retryAttempt;
    private MaxNativeAdLoader nativeAdLoader;
    private MaxAd mnativeAd;


    SharedPreferences prefs;
    Boolean purchased;
    public static final String PURCHASE_KEY = "purchase";
    boolean connected = false;
    boolean doubleBackToExitPressedOnce = false;
    TextView back_info;

    private static final String KEY_LAUGH_COUNT = "key_laugh_count";
    int laughtCount;
    int rating;
    String feedback;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setUpDrawerLayout();

        if (savedInstanceState == null) {
            setMusicChooser(PreferenceUtil.getInstance(this).getLastMusicChooser());
        } else {
            restoreCurrentFragment();
        }

        if (!checkShowIntro()) {
        }
    }

    private void setMusicChooser(int key) {

        PreferenceUtil.getInstance(this).setLastMusicChooser(key);
        switch (key) {
            case LIBRARY:
                navigationView.setCheckedItem(R.id.nav_library);
                setCurrentFragment(LibraryFragment.newInstance());
                break;
            case FOLDERS:
                navigationView.setCheckedItem(R.id.nav_folders);
                setCurrentFragment(FoldersFragment.newInstance(this));
                break;
        }
    }

    private void setCurrentFragment(@SuppressWarnings("NullableProblems") Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    private void restoreCurrentFragment() {
        currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_REQUEST) {
            blockRequestPermissions = false;
            if (!hasPermissions()) {
                requestPermissions();
            }
        }
    }

    @Override
    protected void requestPermissions() {
        if (!blockRequestPermissions) super.requestPermissions();
    }

    @Override
    protected View createContentView() {

        contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = contentView.findViewById(R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        laughtCount = getlaughCount();
        if(laughtCount == 5){ showRateDialog();}
        setLaughCount(laughtCount + 1);
        //Toast.makeText(MainActivity.this, String.valueOf(laughtCount),Toast.LENGTH_SHORT).show();
        purchased = prefs.getBoolean(PURCHASE_KEY, false);

        back_info = contentView.findViewById(R.id.back_info);
        back_info.setBackgroundColor(ThemeStore.primaryColor(this));

        admob_adplaceholder = contentView.findViewById(R.id.admob_adplaceholder);
        admob_adplaceholder.setBackgroundColor(ThemeStore.primaryColor(this));

        max_adplaceholder = contentView.findViewById(R.id.max_adplaceholder);
        max_adplaceholder.setBackgroundColor(ThemeStore.primaryColor(this));

        adRequest = new AdRequest.Builder().build();
        configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration)
                .build();

        if (isOnline()) {
            if (getResources().getString(R.string.ADS_VISIBILITY).equals("NO")) {

                mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
                mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

                banner_ad_unit_id = mFirebaseRemoteConfig.getString("banner_ad_unit_id");
                native_ad_unit_id = mFirebaseRemoteConfig.getString("native_ad_unit_id");
                interstitial_ad_unit_id = mFirebaseRemoteConfig.getString("interstitial_ad_unit_id");
                applovin_native_unit_id = mFirebaseRemoteConfig.getString("applovin_native_unit_id");
                applovin_banner_unit_id = mFirebaseRemoteConfig.getString("applovin_banner_unit_id");
                applovin_interstitial_unit_id = mFirebaseRemoteConfig.getString("applovin_interstitial_unit_id");

                app_version = mFirebaseRemoteConfig.getString("app_version");
                app_info = mFirebaseRemoteConfig.getString("app_info");

                adContainer = contentView.findViewById(R.id.banner_container);
                adView = new AdView(getApplicationContext());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(banner_ad_unit_id);
                adContainer.addView(adView);

                if (!purchased) {
                    Log.i(TAG, "banner_ad_unit_id " + banner_ad_unit_id);
                    Log.i(TAG, "native_ad_unit_id " + native_ad_unit_id);
                    Log.i(TAG, "interstitial_ad_unit_id " + interstitial_ad_unit_id);

                    Log.i(TAG, "applovin_native_unit_id " + applovin_native_unit_id);
                    Log.i(TAG, "applovin_banner_unit_id " + applovin_banner_unit_id);
                    Log.i(TAG, "applovin_interstitial_unit_id " + applovin_interstitial_unit_id);

                    adview();
                    admobNativeAd();
                    createNativeAd();
                    AdmobInterstitial();
                    ApplovinInterstitial();
                }
            }
            else {

                mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
                mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
                mFirebaseRemoteConfig.fetchAndActivate()
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                boolean updated = task.getResult();
                                Log.d(TAG, "Config params updated: " + updated);
                                Log.i(TAG, "Fetch and activate succeeded");
                                //Toast.makeText(InAppBillingActivity.this, "Fetch and activate succeeded",Toast.LENGTH_SHORT).show();

                                banner_ad_unit_id = mFirebaseRemoteConfig.getString("banner_ad_unit_id");
                                native_ad_unit_id = mFirebaseRemoteConfig.getString("native_ad_unit_id");
                                interstitial_ad_unit_id = mFirebaseRemoteConfig.getString("interstitial_ad_unit_id");

                                applovin_native_unit_id = mFirebaseRemoteConfig.getString("applovin_native_unit_id");
                                applovin_banner_unit_id = mFirebaseRemoteConfig.getString("applovin_banner_unit_id");
                                applovin_interstitial_unit_id = mFirebaseRemoteConfig.getString("applovin_interstitial_unit_id");

                                app_version = mFirebaseRemoteConfig.getString("app_version");
                                app_info = mFirebaseRemoteConfig.getString("app_info");

                                SharedPreferences.Editor edit = prefs.edit();
                                edit.putString("interstitial_ad_unit_id", interstitial_ad_unit_id);
                                edit.putString("applovin_native_unit_id", applovin_native_unit_id);
                                edit.putString("applovin_interstitial_unit_id", applovin_interstitial_unit_id);
                                edit.apply();
                            } else {
                                //Toast.makeText(InAppBillingActivity.this, "Fetch failed",Toast.LENGTH_SHORT).show();
                                Log.i(TAG, "Fetch failed");
                            }
                            //Toast.makeText(InAppBillingActivity.this, Interstitial_unit_id,Toast.LENGTH_SHORT).show();

                            Log.i(TAG, "banner_ad_unit_id " + banner_ad_unit_id);
                            Log.i(TAG, "native_ad_unit_id " + native_ad_unit_id);
                            Log.i(TAG, "interstitial_ad_unit_id " + interstitial_ad_unit_id);

                            Log.i(TAG, "applovin_native_unit_id " + applovin_native_unit_id);
                            Log.i(TAG, "applovin_banner_unit_id " + applovin_banner_unit_id);
                            Log.i(TAG, "applovin_interstitial_unit_id " + applovin_interstitial_unit_id);

                            adContainer = contentView.findViewById(R.id.banner_container);
                            adView = new AdView(getApplicationContext());
                            adView.setAdSize(AdSize.BANNER);
                            adView.setAdUnitId(banner_ad_unit_id);
                            adContainer.addView(adView);


                            if (!purchased) {
                                adview();
                                admobNativeAd();
                                createNativeAd();
                                AdmobInterstitial();
                                ApplovinInterstitial();

                            } else {
                                adView.setVisibility(View.GONE);
                            }

                        });
            }
        }
        return contentView;
    }

    public boolean isOnline() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    public void AppInfo() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(
                MainActivity.this);
        // Setting Dialog Title
        //alertDialog.setTitle("Wachi Music Player");
        // Setting Dialog Message
        //alertDialog.getContext().setTheme(colorMap.getPrimaryColorRes());
        alertDialog.setMessage(Html.fromHtml(app_info));

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Update Now",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Update Later",
                (dialog, which) -> {
                    // Write your code here to invoke NO event
                    dialog.cancel();
                });
        // Showing Alert Message
        alertDialog.show();
    }

    public boolean isPrimeNumber(int number) {

        for (int i = 2; i <= number / 2; i++) {
            if (number % i == 0) {
                return false;
            }
        }
        return true;
    }

    public void onBackPressed() {
        laughtCount = getlaughCount();
        if (laughtCount == 5) {
            setLaughCount(laughtCount + 1);
            showRateDialog();
        }
        if (laughtCount == 10) {
            setLaughCount(laughtCount + 1);
            showRateDialog();
        }
        if (laughtCount == 15) {
            setLaughCount(laughtCount + 1);
            showRateDialog();
        }
        if (isOnline()) {

            purchased = prefs.getBoolean(PURCHASE_KEY, false);
            if (!purchased) {
                if (laughtCount > 5) {
                    if (isPrimeNumber(laughtCount)) {
                        if (nativeAdLoader != null) {
                            max_adplaceholder.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            if (nativeAd != null) {
                                admob_adplaceholder.setVisibility(View.VISIBLE);
                                return;
                            }
                        }
                    } else {

                        if (nativeAd != null) {
                            admob_adplaceholder.setVisibility(View.VISIBLE);
                            return;
                        } else {
                            if (nativeAdLoader != null) {
                                max_adplaceholder.setVisibility(View.VISIBLE);
                                return;
                            } else {
                                back_info.setVisibility(View.VISIBLE);
                            }
                        }

                    }
                } else {
                    back_info.setVisibility(View.VISIBLE);
                }
            }
            else {
                admob_adplaceholder.setVisibility(View.GONE);
                max_adplaceholder.setVisibility(View.GONE);
            }

        }
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        //Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        if (nativeAd == null) {
            back_info.setVisibility(View.VISIBLE);
        }
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    public void adview() {

        // Set your test devices. Check your logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345"))
        // to get test ads on this device."
        MobileAds.setRequestConfiguration(new RequestConfiguration.Builder().setTestDeviceIds(Arrays.asList("ABCDEF012345")).build());
        // Gets the ad view defined in layout/ad_fragment.xml with ad unit ID set in
        // values/strings.xml.
        // Create an ad request.
        AdRequest adRequest = new AdRequest.Builder().build();

        // Start loading the ad in the background.
        adView.loadAd(adRequest);
    }



    private void showAdmobInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            interstitialAd.show(MainActivity.this);
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            showApplovinInterstitial();
        }
    }

    private void showApplovinInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (minterstitialAd != null && minterstitialAd.isReady()) {
            minterstitialAd.showAd();
        }

    }

    public void AdmobInterstitial() {

        if (interstitial_ad_unit_id.equals("")) {
            interstitial_ad_unit_id = prefs.getString("interstitial_ad_unit_id", "");
            //Log.i("interstitial_ad_unit_id", interstitial_ad_unit_id);
        }
        InterstitialAd.load(this, interstitial_ad_unit_id, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd aInterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        interstitialAd = aInterstitialAd;

                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, loadAdError.toString());
                        interstitialAd = null;
                    }
                });
    }

    public void ApplovinInterstitial() {
        // Instantiate an InterstitialAd object.
        // NOTE: the placement ID will eventually identify this as your App, you can ignore it for
        // now, while you are testing and replace it later when you have signed up.
        // While you are using this temporary code you will only get test ads and if you release
        // your code like this to the Google Play your users will not receive ads (you will get a no fill error).
        if (applovin_interstitial_unit_id.equals("")) {
            applovin_interstitial_unit_id = prefs.getString("applovin_interstitial_unit_id", "");
        }

        minterstitialAd = new MaxInterstitialAd(applovin_interstitial_unit_id, this);

        MaxAdListener mlistener = new MaxAdListener() {
            @Override
            public void onAdLoaded(MaxAd ad) {
                // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'

                // Reset retry attempt
                retryAttempt = 0;
            }

            @Override
            public void onAdDisplayed(MaxAd ad) {

            }

            @Override
            public void onAdHidden(MaxAd ad) {
                // Interstitial ad is hidden. Pre-load the next ad
                minterstitialAd.loadAd();
            }

            @Override
            public void onAdClicked(MaxAd ad) {

            }

            @Override
            public void onAdLoadFailed(String adUnitId, MaxError error) {
                // Interstitial ad failed to load
                // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)

                retryAttempt++;
                long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

                new Handler().postDelayed(() -> minterstitialAd.loadAd(), delayMillis);
            }

            @Override
            public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                // Interstitial ad failed to display. We recommend loading the next ad
                minterstitialAd.loadAd();
            }
        };

        // For auto play video ads, it's recommended to load the ad
        // at least 30 seconds before it is shown
        if (applovin_interstitial_unit_id != null) {
            try {
                minterstitialAd.setListener(mlistener);
                minterstitialAd.loadAd();

                //	Log.e(TAG, "Interstitial ad Loaded.");
            } catch (Throwable e) {
                // Do nothing, just skip and wait for ad loading
            }
        }

    }


    private void createNativeAd() {
        back_info.setVisibility(View.GONE);
        nativeAdContainer = max_adplaceholder.findViewById(R.id.native_ad_layout);

        if (applovin_native_unit_id.equals("") || applovin_native_unit_id.equals(null)) {
            applovin_native_unit_id = prefs.getString("applovin_native_unit_id", "");
            Log.i("applovin_native_unit_id", applovin_native_unit_id);
        }
        nativeAdLoader = new MaxNativeAdLoader(applovin_native_unit_id, this);
        Log.i("applovin_native_unit_id", applovin_native_unit_id);
        nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
            @Override
            public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                Log.i("applovin_success", "Loaded");
                // Clean up any pre-existing native ad to prevent memory leaks.
                if (mnativeAd != null) {
                    nativeAdLoader.destroy(mnativeAd);
                }

                // Save ad for cleanup.
                mnativeAd = ad;

                // Add ad view to view.
                nativeAdContainer.removeAllViews();
                nativeAdContainer.addView(nativeAdView);
            }

            @Override
            public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                // We recommend retrying with exponentially higher delays up to a maximum delay
                Log.i("applovin_fail", "Failed to Load");
                Log.i("MaxError", adUnitId + ":" + error);
            }

            @Override
            public void onNativeAdClicked(final MaxAd ad) {
                // Optional click callback
            }
        });

        nativeAdLoader.loadAd();

        Button btn_exit = max_adplaceholder.findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button btn_back = max_adplaceholder.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                max_adplaceholder.setVisibility(View.GONE);
            }
        });
    }


    /**
     * Populates a {@link NativeAdView} object with data from a given
     * {@link NativeAd}.
     *
     * @param nativeAd the object containing the ad's assets
     * @param adView   the view to be populated
     */
    private void populateNativeAdView(NativeAd nativeAd, NativeAdView adView) {

        Button btn_exit = adView.findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(v -> finish());
        Button btn_back = adView.findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            if (isOnline()) {
                purchased = prefs.getBoolean(PURCHASE_KEY, false);
                if (!purchased) {
                    admobNativeAd();
                }
            }
            back_info.setVisibility(View.GONE);
            admob_adplaceholder.setVisibility(View.GONE);
        });
        // Set the media view.
        adView.setMediaView(adView.findViewById(R.id.ad_media));

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        adView.setPriceView(adView.findViewById(R.id.ad_price));
        adView.setStarRatingView(adView.findViewById(R.id.ad_stars));
        adView.setStoreView(adView.findViewById(R.id.ad_store));
        adView.setAdvertiserView(adView.findViewById(R.id.ad_advertiser));

        // The headline and mediaContent are guaranteed to be in every UnifiedNativeAd.
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        adView.getMediaView().setMediaContent(nativeAd.getMediaContent());

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd);

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        VideoController vc = Objects.requireNonNull(nativeAd.getMediaContent()).getVideoController();

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
		/*	videoStatus.setText(String.format(Locale.getDefault(),
					"Video status: Ad contains a %.2f:1 video asset.",
					vc.getAspectRatio()));*/

            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                @Override
                public void onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
                    //refresh.setEnabled(true);
                    //videoStatus.setText("Video status: Video playback has ended.");
                    super.onVideoEnd();
                }
            });
        } else {
            //videoStatus.setText("Video status: Ad does not contain a video asset.");
            //refresh.setEnabled(true);
        }
    }

    /**
     * Creates a request for a new native ad based on the boolean parameters and calls the
     * corresponding "populate" method when one is successfully returned.
     *
     */
    private void admobNativeAd() {

        AdLoader.Builder builder = new AdLoader.Builder(this, native_ad_unit_id);

        builder.forNativeAd(aNativeAd -> {
                    // Show the ad.
                    if (isDestroyed()) {
                        aNativeAd.destroy();
                        return;
                    }
                    if (nativeAd != null) {
                        nativeAd.destroy();
                    }
                    nativeAd = aNativeAd;
                    LinearLayout frameLayout =
                            findViewById(R.id.admob_adplaceholder);
                    NativeAdView adView = (NativeAdView) getLayoutInflater()
                            .inflate(R.layout.ad_unified, null);

                    populateNativeAdView(nativeAd, adView);
                    frameLayout.removeAllViews();
                    frameLayout.addView(adView);
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Handle the failure by logging, altering the UI, and so on.
                    }
                })
                .build();


        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {
        }).build();

        adLoader.loadAds(new AdRequest.Builder().build(), 3);


    }

    @Override
    public void onPause() {

        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        laughtCount = getlaughCount();
        if(laughtCount == 5){setLaughCount(laughtCount+1); showRateDialog();}
        if(laughtCount == 10){setLaughCount(laughtCount+1); showRateDialog();}
        if(laughtCount == 15){setLaughCount(laughtCount+1); showRateDialog();}
        if(isOnline()) {

            purchased=prefs.getBoolean(PURCHASE_KEY,false);
            if (!purchased) {

                banner_ad_unit_id = mFirebaseRemoteConfig.getString("banner_ad_unit_id");
                native_ad_unit_id = mFirebaseRemoteConfig.getString("native_ad_unit_id");
                interstitial_ad_unit_id = mFirebaseRemoteConfig.getString("interstitial_ad_unit_id");
                applovin_interstitial_unit_id = mFirebaseRemoteConfig.getString("applovin_interstitial_unit_id");
                applovin_native_unit_id = mFirebaseRemoteConfig.getString("applovin_native_unit_id");
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("applovin_interstitial_unit_id", applovin_interstitial_unit_id);
                edit.apply();
                if (adView != null) {
                    adView.resume();
                }

                AdmobInterstitial();
                ApplovinInterstitial();
                admobNativeAd();


                //Toast.makeText(MainActivity.this, String.valueOf(purchased),Toast.LENGTH_SHORT).show();
            }
            app_version = mFirebaseRemoteConfig.getString("app_version");
            app_info = mFirebaseRemoteConfig.getString("app_info");
            if (Integer.parseInt(app_version) > Integer.parseInt(getResources().getString(R.string.app_version))) {
                if (laughtCount % 2 == 0) {
                    AppInfo();
                }
            }
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if(isOnline()) {
            if (!purchased) {
                if (adView != null) {
                    adView.destroy();
                }

                if (nativeAd != null) {
                    nativeAd.destroy();
                }

                if (minterstitialAd != null) {
                    minterstitialAd.destroy();
                }

            }
        }
        super.onDestroy();
    }
    private void setUpNavigationView() {
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(navigationView, ATHUtil.resolveColor(this, R.attr.iconColor, ThemeStore.textColorSecondary(this)), accentColor);
        NavigationViewUtil.setItemTextColors(navigationView, ThemeStore.textColorPrimary(this), accentColor);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_library:
                    laughtCount = getlaughCount();
                    setLaughCount(laughtCount+1);
                    new Handler().postDelayed(() -> setMusicChooser(LIBRARY), 0);
                    if(isOnline()){
                        if (!purchased) {
                            if(laughtCount > 6) {
                                showAdmobInterstitial();
                            }
                        }
                    }
                    break;
                case R.id.nav_folders:
                    laughtCount = getlaughCount();
                    setLaughCount(laughtCount+1);
                    new Handler().postDelayed(() -> setMusicChooser(FOLDERS), 0);
                    if(isOnline()){
                        if (!purchased) {
                            if(laughtCount > 6) {
                                showApplovinInterstitial();
                            }
                        }
                    }
                    break;
                case R.id.action_scan:
                    laughtCount = getlaughCount();
                    new Handler().postDelayed(() -> {
                        laughtCount = getlaughCount();
                        setLaughCount(laughtCount+1);
                        ScanMediaFolderChooserDialog dialog = ScanMediaFolderChooserDialog.create();
                        dialog.show(getSupportFragmentManager(), "SCAN_MEDIA_FOLDER_CHOOSER");
                    }, 0);
                    if(isOnline()){
                        if (!purchased) {
                            if(laughtCount > 6) {
                                showAdmobInterstitial();
                            }
                        }
                    }
                    break;
                case R.id.nav_equalizer:
                    laughtCount = getlaughCount();
                    setLaughCount(laughtCount+1);
                    NavigationUtil.openEqualizer(this);
                    break;
                case R.id.nav_settings:
                    laughtCount = getlaughCount();
                    setLaughCount(laughtCount+1);
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 0);
                    if(isOnline()){
                        if (!purchased) {
                            if(laughtCount > 6) {
                                showApplovinInterstitial();
                            }
                        }
                    }
                    break;
                case R.id.nav_removeads:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, InAppBillingActivity.class)), 0);
                    break;
                case R.id.nav_rate:
                    showRateDialog();
                    break;
                case R.id.nav_about:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
                    break;
            }
            return true;
        });
    }

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            Song song = MusicPlayerRemote.getCurrentSong();
            if (navigationDrawerHeader == null) {
                navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
                //noinspection ConstantConditions
                navigationDrawerHeader.setOnClickListener(v -> {
                    drawerLayout.closeDrawers();
                    if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        expandPanel();
                    }
                });
            }
            ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
            ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(MusicUtil.getSongInfoString(song));
            SongGlideRequest.Builder.from(Glide.with(this), song)
                    .checkIgnoreMediaStore(this).build()
                    .into(((ImageView) navigationDrawerHeader.findViewById(R.id.image)));
        } else {
            if (navigationDrawerHeader != null) {
                navigationView.removeHeaderView(navigationDrawerHeader);
                navigationDrawerHeader = null;
            }
        }
    }

    @Override
    public void onPlayingMetaChanged() {
        super.onPlayingMetaChanged();
        updateNavigationDrawerHeader();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateNavigationDrawerHeader();
        handlePlaybackIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }
        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    private void handlePlaybackIntent(@Nullable Intent intent) {
        if (intent == null) {
            return;
        }

        Uri uri = intent.getData();
        String mimeType = intent.getType();
        boolean handled = false;

        if (intent.getAction() != null && intent.getAction().equals(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH)) {
            final List<Song> songs = SearchQueryHelper.getSongs(this, intent.getExtras());
            if (MusicPlayerRemote.getShuffleMode() == MusicService.SHUFFLE_MODE_SHUFFLE) {
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
            } else {
                MusicPlayerRemote.openQueue(songs, 0, true);
            }
            handled = true;
        }

        if (uri != null && uri.toString().length() > 0) {
            MusicPlayerRemote.playFromUri(uri);
            handled = true;
        } else if (MediaStore.Audio.Playlists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "playlistId", "playlist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                List<Song> songs = new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(this, id));
                MusicPlayerRemote.openQueue(songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Albums.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "albumId", "album");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(AlbumLoader.getAlbum(this, id).songs, position, true);
                handled = true;
            }
        } else if (MediaStore.Audio.Artists.CONTENT_TYPE.equals(mimeType)) {
            final long id = parseIdFromIntent(intent, "artistId", "artist");
            if (id >= 0) {
                int position = intent.getIntExtra("position", 0);
                MusicPlayerRemote.openQueue(ArtistLoader.getArtist(this, id).getSongs(), position, true);
                handled = true;
            }
        }
        if (handled) {
            setIntent(new Intent());
        }
    }

    private long parseIdFromIntent(@NonNull Intent intent, String longKey,
                                   String stringKey) {
        long id = intent.getLongExtra(longKey, -1);
        if (id < 0) {
            String idString = intent.getStringExtra(stringKey);
            if (idString != null) {
                try {
                    id = Long.parseLong(idString);
                } catch (NumberFormatException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return id;
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private boolean checkShowIntro() {
        if (!PreferenceUtil.getInstance(this).introShown()) {
            PreferenceUtil.getInstance(this).setIntroShown();
            blockRequestPermissions = true;
            new Handler().postDelayed(() -> startActivityForResult(new Intent(MainActivity.this, AppIntroActivity.class), APP_INTRO_REQUEST), 50);
            return true;
        }
        return false;
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }
    public int getlaughCount() {
        return prefs.getInt(KEY_LAUGH_COUNT ,0);
    }

    public void setLaughCount(final int i) {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAUGH_COUNT, i);
        editor.apply();
    }
    @SuppressLint("ResourceType")
    public  void showRateDialog() {
        SmartRate.Rate(MainActivity.this
                , "Rate App"
                , "I am a developer working very hard to give you the best free application. " +
                        "Your 5-Star review means a lot to me. Please write a nice review in Google Play to encourage me!"
                , "Continue"
                , "Please take a moment and rate this app 5-stars on Google Play"
                , "click here"
                , "Ask me later"
                , "Never ask again"
                , "Cancel"
                , "Please provide feedback."
                , ThemeStore.primaryColor(this)
                , 5
                , 0
                , 0
                ,new SmartRate.CallBack_UserRating() {
                    @Override
                    public void userRating(int rate) {
                        //	Toast.makeText(SettingsActivity.this, "Rating: " + rating + " Stars", Toast.LENGTH_LONG).show();
                        if(rate<5){
                            laughtCount = getlaughCount();
                            setLaughCount(laughtCount+20);
                            rating=rate;
                            showFeedbackDialog();

                        }
                        //saveUserRating(rating);
                    }
                }
        );


    }
    @SuppressLint("ResourceType")
    private void showFeedbackDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.getContext().setTheme(ThemeStore.primaryColor(this));
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        final TextView txt_rating=dialog.findViewById(R.id.txt_rating);
        txt_rating.setText("Your Rating "+rating+" Stars");
        final EditText edt_feedback=dialog.findViewById(R.id.txt_feedback);
        edt_feedback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        dialog.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedback=edt_feedback.getText().toString();
                if(feedback.length()==0){
                    edt_feedback.setError("Please write feedback");
                    Toast.makeText(MainActivity.this, "Please write feedback!", Toast.LENGTH_LONG).show();
                    return;
                }
                requestFeature();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void requestFeature() {
        try {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request)
            );
            email.putExtra(Intent.EXTRA_TEXT, feedback);

            startActivity(Intent.createChooser(email, getResources().getString(R.string.send_email)));
        } catch (android.content.ActivityNotFoundException ex) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request));
            email.putExtra(Intent.EXTRA_TEXT, feedback);
            Intent chooser = Intent.createChooser(email, getResources().getString(R.string.send_email));
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(email);
            Toast.makeText(MainActivity.this, "Email not Found", Toast.LENGTH_SHORT).show();
        }
    }

    private void requestFeatureOld() {
        try {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            final PackageManager pm = this.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(email, 0);
            String className = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                    className = info.activityInfo.name;

                    if(className != null && !className.isEmpty()){
                        break;
                    }
                }
            }
            //Explicitly only use Gmail to send
            email.setClassName("com.google.android.gm",className);
            email.setType("plain/text");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request)
            );
            email.putExtra(Intent.EXTRA_TEXT, feedback);

            startActivity(email);
        } catch (android.content.ActivityNotFoundException ex) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request));
            email.putExtra(Intent.EXTRA_TEXT, feedback);
            Intent chooser = Intent.createChooser(email, getResources().getString(R.string.send_email));
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(email);

        }
    }

    public static String getAppVersion(Context context) {
        String versionName;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }
        return versionName;
    }
}
