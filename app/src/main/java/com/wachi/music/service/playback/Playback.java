package com.wachi.music.service.playback;

import androidx.annotation.Nullable;

public interface Playback {

    boolean setDataSource(String path);

    void setNextDataSource(@Nullable String path);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isInitialized();

    boolean start();

    void stop();

    void release();

    boolean pause();

    boolean isPlaying();

    int duration();

    int position();

    int seek(int whereto);

    boolean setVolume(float vol);

    boolean setAudioSessionId(int sessionId);

    int getAudioSessionId();

    interface PlaybackCallbacks {
        void onSongWentToNext();

        void onSongEnded();
    }
}
