package com.bourke.glimmr.fragments.home;

import android.content.Intent;

import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;

import com.bourke.glimmr.activities.PhotosetViewerActivity;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.event.Events.IPhotosetsReadyListener;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.R;
import com.bourke.glimmr.tasks.LoadPhotosetsTask;

import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PhotosetsFragment extends BaseFragment
        implements IPhotosetsReadyListener {

    private static final String TAG = "Glimmr/PhotosetsFragment";

    private List<Photoset> mPhotosets = new ArrayList<Photoset>();

    public static PhotosetsFragment newInstance() {
        return new PhotosetsFragment();
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadPhotosetsTask(mActivity, this).execute(mOAuth);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (RelativeLayout) inflater.inflate(R.layout
                .standard_list_fragment, container, false);
        return mLayout;
    }

    private void startPhotosetViewer(Photoset photoset) {
        if (photoset == null) {
            Log.e(getLogTag(),
                    "Cannot start SetViewerActivity, photoset is null");
            return;
        }
        Log.d(getLogTag(), "Starting SetViewerActivity for "
                + photoset.getTitle());
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_PHOTOSETVIEWER_PHOTOSET,
                photoset);
        Intent photosetViewer = new Intent(mActivity, PhotosetViewerActivity
                .class);
        photosetViewer.putExtras(bundle);
        mActivity.startActivity(photosetViewer);
    }

    public void itemClicked(AdapterView<?> parent, View view, int position,
            long id) {
        startPhotosetViewer(mPhotosets.get(position));
    }

    @Override
    public void onPhotosetsReady(Photosets photoSets) {
        Log.d(getLogTag(), "onPhotosetListReady");
        mGridAq = new AQuery(mActivity, mLayout);
        mPhotosets = new ArrayList(photoSets.getPhotosets());

        ArrayAdapter<Photoset> adapter = new ArrayAdapter<Photoset>(mActivity,
                R.layout.photoset_list_item, (ArrayList<Photoset>)mPhotosets) {

            // TODO: implement ViewHolder pattern
            // TODO: add aquery delay loading for fling scrolling
            @Override
            public View getView(final int position, View convertView,
                    ViewGroup parent) {

                if (convertView == null) {
                    convertView = mActivity.getLayoutInflater().inflate(
                            R.layout.photoset_list_item, null);
                }

                final Photoset photoset = mPhotosets.get(position);
                AQuery aq = mGridAq.recycle(convertView);

                aq.id(R.id.image_item).image(photoset.getPrimaryPhoto()
                        .getMediumUrl(), true, true, 0, 0, null,
                        AQuery.FADE_IN_NETWORK);
                aq.id(R.id.set_name_text).text(photoset.getTitle());
                aq.id(R.id.num_images_text).text(""+photoset.getPhotoCount());

                return convertView;
            }
        };
        mGridAq.id(R.id.list).adapter(adapter).itemClicked(this,
                "itemClicked");
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
