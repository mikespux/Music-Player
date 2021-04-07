package com.wachi.musicplayer.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.bumptech.glide.Glide;
import com.github.ksoichiro.android.observablescrollview.ObservableListView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.wachi.musicplayer.R;
import com.wachi.musicplayer.adapter.album.HorizontalAlbumAdapter;
import com.wachi.musicplayer.adapter.song.ArtistSongAdapter;
import com.wachi.musicplayer.dialogs.AddToPlaylistDialog;
import com.wachi.musicplayer.dialogs.SleepTimerDialog;
import com.wachi.musicplayer.glide.ArtistGlideRequest;
import com.wachi.musicplayer.glide.MusicColoredTarget;
import com.wachi.musicplayer.helper.MusicPlayerRemote;
import com.wachi.musicplayer.interfaces.CabHolder;
import com.wachi.musicplayer.interfaces.LoaderIds;
import com.wachi.musicplayer.interfaces.PaletteColorHolder;
import com.wachi.musicplayer.lastfm.rest.LastFMRestClient;
import com.wachi.musicplayer.lastfm.rest.model.LastFmArtist;
import com.wachi.musicplayer.loader.ArtistLoader;
import com.wachi.musicplayer.misc.SimpleObservableScrollViewCallbacks;
import com.wachi.musicplayer.misc.WrappedAsyncTaskLoader;
import com.wachi.musicplayer.model.Artist;
import com.wachi.musicplayer.model.Song;
import com.wachi.musicplayer.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.wachi.musicplayer.util.CustomArtistImageUtil;
import com.wachi.musicplayer.util.MusicColorUtil;
import com.wachi.musicplayer.util.MusicUtil;
import com.wachi.musicplayer.util.NavigationUtil;
import com.wachi.musicplayer.util.PreferenceUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Be careful when changing things in this Activity!
 */
public class ArtistDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder, LoaderManager.LoaderCallbacks<Artist> {

    private static final int LOADER_ID = LoaderIds.ARTIST_DETAIL_ACTIVITY;
    private static final int REQUEST_CODE_SELECT_IMAGE = 1000;

    public static final String EXTRA_ARTIST_ID = "extra_artist_id";

    @BindView(R.id.list)
    ObservableListView songListView;
    @BindView(R.id.image)
    ImageView artistImage;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.header)
    View headerView;
    @BindView(R.id.header_overlay)
    View headerOverlay;

    @BindView(R.id.duration_icon)
    ImageView durationIconImageView;
    @BindView(R.id.song_count_icon)
    ImageView songCountIconImageView;
    @BindView(R.id.album_count_icon)
    ImageView albumCountIconImageView;
    @BindView(R.id.duration_text)
    TextView durationTextView;
    @BindView(R.id.song_count_text)
    TextView songCountTextView;
    @BindView(R.id.album_count_text)
    TextView albumCountTextView;

    View songListHeader;
    RecyclerView albumRecyclerView;

    private MaterialCab cab;
    private int headerViewHeight;
    private int toolbarColor;

    private Artist artist;
    @Nullable
    private Spanned biography;
    private MaterialDialog biographyDialog;
    private HorizontalAlbumAdapter albumAdapter;
    private ArtistSongAdapter songAdapter;

    private LastFMRestClient lastFMRestClient;

    private final SimpleObservableScrollViewCallbacks observableScrollViewCallbacks = new SimpleObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            scrollY += headerViewHeight;

            // Change alpha of overlay
            float headerAlpha = Math.max(0, Math.min(1, (float) 2 * scrollY / headerViewHeight));
            headerOverlay.setBackgroundColor(ColorUtil.withAlpha(toolbarColor, headerAlpha));

            // Translate name text
            headerView.setTranslationY(Math.max(-scrollY, -headerViewHeight));
            headerOverlay.setTranslationY(Math.max(-scrollY, -headerViewHeight));
            artistImage.setTranslationY(Math.max(-scrollY, -headerViewHeight));
        }
    };


    public static final String PURCHASE_KEY = "purchase";
    private final FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings;
    // cache expiration in seconds
    long cacheExpiration = 3600;
    String banner_ad_unit_id = "";
    // Find the Ad Container
    LinearLayout adContainer;
    boolean connected = false;
    SharedPreferences prefs;
    Boolean purchased;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        lastFMRestClient = new LastFMRestClient(this);
        usePalette = PreferenceUtil.getInstance(this).albumArtistColoredFooters();

        initViews();
        setUpObservableListViewParams();
        setUpToolbar();
        setUpViews();

        getSupportLoaderManager().initLoader(LOADER_ID, getIntent().getExtras(), this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        purchased = prefs.getBoolean(PURCHASE_KEY, false);
        configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(cacheExpiration)
                .build();

        if (isOnline()) {
            if (getResources().getString(R.string.ADS_VISIBILITY).equals("NO")) {

                mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
                mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

                banner_ad_unit_id = mFirebaseRemoteConfig.getString("banner_ad_unit_id");

                adContainer = findViewById(R.id.banner_container);
                adView = new AdView(getApplicationContext());
                adView.setAdSize(AdSize.BANNER);
                adView.setAdUnitId(banner_ad_unit_id);
                adContainer.addView(adView);

                if (!purchased) {
                    adview();
                }
            } else {

                mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);
                mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);
                mFirebaseRemoteConfig.fetchAndActivate()
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                boolean updated = task.getResult();
                                //Toast.makeText(InAppBillingActivity.this, "Fetch and activate succeeded",Toast.LENGTH_SHORT).show();

                                banner_ad_unit_id = mFirebaseRemoteConfig.getString("banner_ad_unit_id");

                            } else {
                                //Toast.makeText(InAppBillingActivity.this, "Fetch failed",Toast.LENGTH_SHORT).show();

                            }
                            //Toast.makeText(InAppBillingActivity.this, Interstitial_unit_id,Toast.LENGTH_SHORT).show();


                            adContainer = findViewById(R.id.banner_container);
                            adView = new AdView(getApplicationContext());
                            adView.setAdSize(AdSize.BANNER);
                            adView.setAdUnitId(banner_ad_unit_id);
                            adContainer.addView(adView);


                            if (!purchased) {
                                adview();

                            } else {
                                adView.setVisibility(View.GONE);
                            }

                        });
            }
        }
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

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (isOnline()) {
            purchased = prefs.getBoolean(PURCHASE_KEY, false);
            if (!purchased) {
                if (adView != null) {
                    adView.resume();
                }
            }

        }
    }

    @Override
    protected void onDestroy() {
        if (isOnline()) {
            if (!purchased) {
                if (adView != null) {
                    adView.destroy();
                }

            }
        }
        super.onDestroy();
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_artist_detail);
    }

    private boolean usePalette;

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void initViews() {
        songListHeader = LayoutInflater.from(this).inflate(R.layout.artist_detail_header, songListView, false);
        albumRecyclerView = songListHeader.findViewById(R.id.recycler_view);
    }

    private void setUpViews() {
        setUpSongListView();
        setUpAlbumRecyclerView();
        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void setUpSongListView() {
        setUpSongListPadding();
        songListView.setScrollViewCallbacks(observableScrollViewCallbacks);
        songListView.addHeaderView(songListHeader);

        songAdapter = new ArtistSongAdapter(this, getArtist().getSongs(), this);
        songListView.setAdapter(songAdapter);

        final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
        contentView.post(() -> observableScrollViewCallbacks.onScrollChanged(-headerViewHeight, false, false));
    }

    private void setUpSongListPadding() {
        songListView.setPadding(0, headerViewHeight, 0, 0);
    }

    private void setUpAlbumRecyclerView() {
        albumRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new HorizontalAlbumAdapter(this, getArtist().albums, usePalette, this);
        albumRecyclerView.setAdapter(albumAdapter);
        albumAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (albumAdapter.getItemCount() == 0) finish();
            }
        });
    }

    protected void setUsePalette(boolean usePalette) {
        albumAdapter.usePalette(usePalette);
        PreferenceUtil.getInstance(this).setAlbumArtistColoredFooters(usePalette);
        this.usePalette = usePalette;
    }

    private void reload() {
        getSupportLoaderManager().restartLoader(LOADER_ID, getIntent().getExtras(), this);
    }

    private void loadBiography() {
        loadBiography(Locale.getDefault().getLanguage());
    }

    private void loadBiography(@Nullable final String lang) {
        biography = null;

        lastFMRestClient.getApiService()
                .getArtistInfo(getArtist().getName(), lang, null)
                .enqueue(new Callback<LastFmArtist>() {
                    @Override
                    public void onResponse(@NonNull Call<LastFmArtist> call, @NonNull Response<LastFmArtist> response) {
                        final LastFmArtist lastFmArtist = response.body();
                        if (lastFmArtist != null && lastFmArtist.getArtist() != null) {
                            final String bioContent = lastFmArtist.getArtist().getBio().getContent();
                            if (bioContent != null && !bioContent.trim().isEmpty()) {
                                biography = Html.fromHtml(bioContent);
                            }
                        }

                        // If the "lang" parameter is set and no biography is given, retry with default language
                        if (biography == null && lang != null) {
                            loadBiography(null);
                            return;
                        }

                        if (!PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) {
                            if (biography != null) {
                                biographyDialog.setContent(biography);
                            } else {
                                biographyDialog.dismiss();
                                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<LastFmArtist> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        biography = null;
                    }
                });
    }

    private void loadArtistImage() {
        ArtistGlideRequest.Builder.from(Glide.with(this), artist)
                .generatePalette(this).build()
                .dontAnimate()
                .into(new MusicColoredTarget(artistImage) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    CustomArtistImageUtil.getInstance(this).setCustomArtistImage(artist, data.getData());
                }
                break;
            default:
                if (resultCode == RESULT_OK) {
                    reload();
                }
                break;
        }
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setColors(int color) {
        toolbarColor = color;
        headerView.setBackgroundColor(color);

        setNavigationbarColor(color);
        setTaskDescriptionColor(color);

        toolbar.setBackgroundColor(color);
        setSupportActionBar(toolbar); // needed to auto readjust the toolbar content color
        setStatusbarColor(color);

        int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        durationIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        songCountIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        albumCountIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        durationTextView.setTextColor(secondaryTextColor);
        songCountTextView.setTextColor(secondaryTextColor);
        albumCountTextView.setTextColor(secondaryTextColor);
    }

    private void setUpToolbar() {
        toolbar.setTitleTextAppearance(this, R.style.ProductSansTextAppearance);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        menu.findItem(R.id.action_colored_footers).setChecked(usePalette);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final List<Song> songs = songAdapter.getDataSet();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_equalizer:
                NavigationUtil.openEqualizer(this);
                return true;
            case R.id.action_shuffle_artist:
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_current_playing:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_biography:
                if (biographyDialog == null) {
                    biographyDialog = new MaterialDialog.Builder(this)
                            .title(artist.getName())
                            .positiveText(android.R.string.ok)
                            .build();
                }
                if (PreferenceUtil.isAllowedToDownloadMetadata(ArtistDetailActivity.this)) { // wiki should've been already downloaded
                    if (biography != null) {
                        biographyDialog.setContent(biography);
                        biographyDialog.show();
                    } else {
                        Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.biography_unavailable), Toast.LENGTH_SHORT).show();
                    }
                } else { // force download
                    biographyDialog.show();
                    loadBiography();
                }
                return true;
            case R.id.action_set_artist_image:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_from_local_storage)), REQUEST_CODE_SELECT_IMAGE);
                return true;
            case R.id.action_reset_artist_image:
                Toast.makeText(ArtistDetailActivity.this, getResources().getString(R.string.updating), Toast.LENGTH_SHORT).show();
                CustomArtistImageUtil.getInstance(ArtistDetailActivity.this).resetCustomArtistImage(artist);
                return true;
            case R.id.action_colored_footers:
                item.setChecked(!item.isChecked());
                setUsePalette(item.isChecked());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        songAdapter.setColor(getPaletteColor());
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(MusicColorUtil.shiftBackgroundColorForLightText(getPaletteColor()))
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            albumRecyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        reload();
    }

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setArtist(Artist artist) {
        this.artist = artist;
        loadArtistImage();

        if (PreferenceUtil.isAllowedToDownloadMetadata(this)) {
            loadBiography();
        }

        getSupportActionBar().setTitle(artist.getName());
        songCountTextView.setText(MusicUtil.getSongCountString(this, artist.getSongCount()));
        albumCountTextView.setText(MusicUtil.getAlbumCountString(this, artist.getAlbumCount()));
        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, artist.getSongs())));

        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.albums);
    }

    private Artist getArtist() {
        if (artist == null) artist = new Artist();
        return artist;
    }

    @Override
    public Loader<Artist> onCreateLoader(int id, Bundle args) {
        return new AsyncArtistDataLoader(this, args.getLong(EXTRA_ARTIST_ID));
    }

    @Override
    public void onLoadFinished(Loader<Artist> loader, Artist data) {
        setArtist(data);
    }

    @Override
    public void onLoaderReset(Loader<Artist> loader) {
        this.artist = new Artist();
        songAdapter.swapDataSet(artist.getSongs());
        albumAdapter.swapDataSet(artist.albums);
    }

    private static class AsyncArtistDataLoader extends WrappedAsyncTaskLoader<Artist> {
        private final long artistId;

        public AsyncArtistDataLoader(Context context, long artistId) {
            super(context);
            this.artistId = artistId;
        }

        @Override
        public Artist loadInBackground() {
            return ArtistLoader.getArtist(getContext(), artistId);
        }
    }
}
