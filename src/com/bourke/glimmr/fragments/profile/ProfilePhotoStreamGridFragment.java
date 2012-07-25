package com.bourke.glimmr.fragments.profile;

import com.googlecode.flickrjandroid.people.User;
import com.bourke.glimmr.fragments.base.ProfilePhotoGridFragment;
import com.bourke.glimmr.tasks.LoadPhotostreamTask;
import com.bourke.glimmr.tasks.LoadUserTask;

public class ProfilePhotoStreamGridFragment extends ProfilePhotoGridFragment {

    private static final String TAG = "Glimmr/ProfilePhotoStreamGridFragment";

    private int mPage = 1;

    public static ProfilePhotoStreamGridFragment newInstance(User user) {
        ProfilePhotoStreamGridFragment newFragment =
            new ProfilePhotoStreamGridFragment();
        newFragment.mUser = user;
        return newFragment;
    }

    @Override
    protected void startTask() {
        super.startTask();
        new LoadPhotostreamTask(mActivity, this, mUser, mPage++)
            .execute(mOAuth);
        new LoadUserTask(mActivity, this, mUser).execute(mOAuth);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }
}
