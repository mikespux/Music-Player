package com.wachi.musicplayer.glide.artistimage;

import android.content.Context;

import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;
import com.wachi.musicplayer.util.PreferenceUtil;

import java.io.InputStream;

public class ArtistImageLoader implements StreamModelLoader<ArtistImage> {
    private final Context context;

    public ArtistImageLoader(Context context) {
        this.context = context;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(final ArtistImage model, int width, int height) {
        return new ArtistImageFetcher(model, PreferenceUtil.getInstance(context).ignoreMediaStoreArtwork());
    }

    public static class Factory implements ModelLoaderFactory<ArtistImage, InputStream> {

        @Override
        public ModelLoader<ArtistImage, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new ArtistImageLoader(context);
        }

        @Override
        public void teardown() {

        }
    }
}

