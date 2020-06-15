package com.wachi.music.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;

import androidx.annotation.NonNull;

import com.wachi.music.R;
import com.wachi.music.loader.LastAddedLoader;
import com.wachi.music.model.Song;

import java.util.List;

public class LastAddedPlaylist extends AbsSmartPlaylist {

    public LastAddedPlaylist(@NonNull Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_library_add_white_24dp);
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull Context context) {
        return LastAddedLoader.getLastAddedSongs(context);
    }

    @Override
    public void clear(@NonNull Context context) {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected LastAddedPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<LastAddedPlaylist> CREATOR = new Creator<LastAddedPlaylist>() {
        public LastAddedPlaylist createFromParcel(Parcel source) {
            return new LastAddedPlaylist(source);
        }

        public LastAddedPlaylist[] newArray(int size) {
            return new LastAddedPlaylist[size];
        }
    };
}
