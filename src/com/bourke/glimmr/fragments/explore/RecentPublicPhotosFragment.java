package com.bourke.glimmr.fragments.explore;

import android.os.Bundle;

import com.bourke.glimmr.event.Events.IPhotoListReadyListener;
import com.bourke.glimmr.fragments.base.PhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPublicPhotosTask;

import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

public class RecentPublicPhotosFragment extends PhotoGridFragment
        implements IPhotoListReadyListener {

    private static final String TAG = "Glimmr/RecentPublicPhotosFragment";

    private LoadPublicPhotosTask mTask;

    public static RecentPublicPhotosFragment newInstance() {
        return new RecentPublicPhotosFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShowDetailsOverlay = false;
    }

    /**
     * Once the parent binds the adapter it will trigger cacheInBackground
     * for us as it will be empty when first bound.
     */
    @Override
    protected boolean cacheInBackground() {
        startTask(mPage++);
        return mMorePages;
    }

    private void startTask(int page) {
        super.startTask();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPublicPhotosTask(this, page);
        mTask.execute();
    }

    @Override
    protected void refresh() {
        super.refresh();
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.TRUE);
        mTask = new LoadPublicPhotosTask(this, mPage);
        mTask.execute();
    }

    @Override
    public void onPhotosReady(List<Photo> photos) {
        super.onPhotosReady(photos);
        mActivity.setSupportProgressBarIndeterminateVisibility(Boolean.FALSE);
        if (photos != null && photos.isEmpty()) {
            mMorePages = false;
        }
    }

    @Override
    public String getNewestPhotoId() {
        /* Won't be notifying about new public photos */
        return null;
    }

    @Override
    public void storeNewestPhotoId(Photo photo) {
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
