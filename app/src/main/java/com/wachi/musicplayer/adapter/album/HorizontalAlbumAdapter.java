package com.wachi.musicplayer.adapter.album;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.wachi.musicplayer.glide.MusicColoredTarget;
import com.wachi.musicplayer.glide.SongGlideRequest;
import com.wachi.musicplayer.helper.HorizontalAdapterHelper;
import com.wachi.musicplayer.interfaces.CabHolder;
import com.wachi.musicplayer.model.Album;
import com.wachi.musicplayer.util.MusicUtil;

import java.util.List;

public class HorizontalAlbumAdapter extends AlbumAdapter {

    public HorizontalAlbumAdapter(@NonNull AppCompatActivity activity, List<Album> dataSet, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, usePalette, cabHolder);
    }

    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType);
        return new ViewHolder(view);
    }

    @Override
    protected void setColors(int color, ViewHolder holder) {
        if (holder.itemView != null) {
            CardView card=(CardView)holder.itemView;
            card.setCardBackgroundColor(color);
            if (holder.title != null) {
                    holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
            if (holder.text != null) {
                    holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    @Override
    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        SongGlideRequest.Builder.from(Glide.with(activity), album.safeGetFirstSong())
                .checkIgnoreMediaStore(activity)
                .generatePalette(activity).build()
                .into(new MusicColoredTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getAlbumArtistFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette)
                            setColors(color, holder);
                        else
                            setColors(getAlbumArtistFooterColor(), holder);
                    }
                });
    }

    @Override
    protected String getAlbumText(Album album) {
        return MusicUtil.getYearString(album.getYear());
    }

    @Override
    public int getItemViewType(int position) {
        return HorizontalAdapterHelper.getItemViewtype(position, getItemCount());
    }
}
