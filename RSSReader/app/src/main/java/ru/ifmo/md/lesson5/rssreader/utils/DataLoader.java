package ru.ifmo.md.lesson5.rssreader.utils;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by Nikita Yaschenko on 21.10.14.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
    private D mData;

    public DataLoader(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(D data) {
        mData = data;
        if (isStarted()) {
            super.deliverResult(data);
        }
    }
}
