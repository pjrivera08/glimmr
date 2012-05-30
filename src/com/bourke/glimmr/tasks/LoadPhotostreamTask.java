package com.bourke.glimmr;

import android.app.Activity;

import android.os.AsyncTask;

import android.widget.GridView;

import com.fedorvlasov.lazylist.LazyAdapter;

import com.gmail.yuyang226.flickr.Flickr;
import com.gmail.yuyang226.flickr.oauth.OAuth;
import com.gmail.yuyang226.flickr.oauth.OAuthToken;
import com.gmail.yuyang226.flickr.people.User;
import com.gmail.yuyang226.flickr.photos.PhotoList;

import java.util.HashSet;
import java.util.Set;

public class LoadPhotostreamTask extends AsyncTask<OAuth, Void, PhotoList> {

	private GridView mGridView;
	private Activity activity;

	public LoadPhotostreamTask(Activity activity, GridView mGridView) {
		this.activity = activity;
		this.mGridView = mGridView;
	}

	@Override
	protected PhotoList doInBackground(OAuth... arg0) {
		OAuthToken token = arg0[0].getToken();
		Flickr f = FlickrHelper.getInstance().getFlickrAuthed(
                token.getOauthToken(), token.getOauthTokenSecret());
		Set<String> extras = new HashSet<String>();
		extras.add("url_sq");
		extras.add("url_l");
		extras.add("views");
		User user = arg0[0].getUser();
		try {
			return f.getPeopleInterface().getPhotos(user.getId(), extras, 20,
                    1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(PhotoList result) {
		if (result != null) {
			LazyAdapter adapter = new LazyAdapter(this.activity);
			mGridView.setAdapter(adapter);
		}
	}
}
