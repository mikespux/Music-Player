package com.wachi.musicplayer.misc;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

public abstract class WeakContextAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
    private WeakReference<Context> contextWeakReference;

    public WeakContextAsyncTask(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    @Nullable
    protected Context getContext() {
        return contextWeakReference.get();
    }
}
