package com.bourke.glimmr;

import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Bundle;

import android.support.v4.view.ViewPager;

import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import com.androidquery.AQuery;

import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import android.content.Context;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;

public abstract class BaseActivity extends SherlockFragmentActivity
        implements ViewPager.OnPageChangeListener {

    private static final String TAG = "Glimmr/BaseActivity";

    /**
     * Should contain current user and valid access token for that user.
     */
    protected OAuth mOAuth;
    protected AQuery mAq;
    protected int mStackLevel = 0;

    private MenuItem mMenuItemProgress;
    private MenuItem mMenuItemRefresh;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME,
                Context.MODE_PRIVATE);
        mOAuth = loadAccessToken(prefs);
    }

    protected static OAuth loadAccessToken(SharedPreferences prefs) {
        String oauthTokenString = prefs.getString(Constants.KEY_OAUTH_TOKEN,
                null);
        String tokenSecret = prefs.getString(Constants.KEY_TOKEN_SECRET, null);
        String userName = prefs.getString(Constants.KEY_USER_NAME, null);
        String userId = prefs.getString(Constants.KEY_USER_ID, null);

        OAuth oauth = null;
        if (oauthTokenString != null && tokenSecret != null && userName != null
                && userId != null) {
            oauth = new OAuth();
            OAuthToken oauthToken = new OAuthToken();
            oauth.setToken(oauthToken);
            oauthToken.setOauthToken(oauthTokenString);
            oauthToken.setOauthTokenSecret(tokenSecret);

            User user = new User();
        	user.setUsername(userName);
        	user.setId(userId);
        	oauth.setUser(user);
        } else {
            Log.w(TAG, "No saved oauth token found");
            return null;
        }
        return oauth;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main_menu, menu);
        mMenuItemProgress = menu.findItem(R.id.menu_progress);
        mMenuItemRefresh = menu.findItem(R.id.menu_refresh);
        return true;
    }

    public void showProgressIcon(boolean show) {
        if (mMenuItemProgress != null && mMenuItemRefresh != null) {
            if(show) {
                Log.d(TAG, "showProgressIcon(true)");
                mMenuItemProgress.setVisible(true);
                mMenuItemRefresh.setVisible(false);
            }
            else {
                Log.d(TAG, "showProgressIcon(false)");
                mMenuItemRefresh.setVisible(true);
                mMenuItemProgress.setVisible(false);
            }
        } else {
            Log.d(TAG, "showProgressIcon: mMenuItemProgress/mMenuItemRefresh "
                    + "null");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onPageScrolled(int pos, float posOffset, int posOffsetPx) {}

    @Override
    public void onPageSelected(int pos) {}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("level", mStackLevel);
    }
}