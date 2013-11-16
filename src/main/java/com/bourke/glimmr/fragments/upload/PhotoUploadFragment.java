package com.bourke.glimmr.fragments.upload;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.bourke.glimmr.R;
import com.bourke.glimmr.common.Constants;
import com.bourke.glimmr.common.GsonHelper;
import com.bourke.glimmr.common.TextUtils;
import com.bourke.glimmr.event.Events;
import com.bourke.glimmr.fragments.base.BaseFragment;
import com.bourke.glimmr.tasks.LoadPhotosetsTask;
import com.google.gson.Gson;
import com.googlecode.flickrjandroid.photosets.Photoset;
import com.googlecode.flickrjandroid.photosets.Photosets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PhotoUploadFragment extends BaseFragment {

    public static final String TAG = "Glimmr/PhotoUploadFragment";

    public static final String KEY_PHOTO = "com.bourke.glimmr.PhotoUploadFragment.KEY_PHOTO";

    private LocalPhotosGridFragment.LocalPhoto mPhoto;
    private EditText mEditTextTitle;
    private EditText mEditTextDescription;
    private Switch mSwitchIsPublic;
    private EditText mEditTextTags;
    private EditText mEditTextSets;
    private Photosets mPhotosets;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        new GsonHelper(mActivity).marshallObject(mPhoto, outState, KEY_PHOTO);
    }

    @Override
    public void onActivityCreated(Bundle inState) {
        super.onActivityCreated(inState);
        if (inState != null && mPhoto == null) {
            String json = inState.getString(KEY_PHOTO, "");
            mPhoto = new Gson().fromJson(json, LocalPhotosGridFragment.LocalPhoto.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mLayout = (ScrollView) inflater.inflate(R.layout.photo_upload_fragment, container, false);

        TextView basicSectionTitle = (TextView) mLayout.findViewById(R.id.textViewBasicSection);
        mTextUtils.setFont(basicSectionTitle, TextUtils.FONT_ROBOTOTHIN);

        mEditTextTitle = (EditText) mLayout.findViewById(R.id.editTextTitle);
        mEditTextTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    mPhoto.getMetadata().setTitle(((TextView) view).getText().toString());
                }
            }
        });

        mEditTextDescription = (EditText) mLayout.findViewById(R.id.editTextDescription);
        mEditTextDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    mPhoto.getMetadata().setDescription(((TextView) view).getText().toString());
                }
            }
        });

        TextView advancedSectionTitle = (TextView)
                mLayout.findViewById(R.id.textViewAdvancedSection);
        mTextUtils.setFont(advancedSectionTitle, TextUtils.FONT_ROBOTOTHIN);

        mSwitchIsPublic = (Switch) mLayout.findViewById(R.id.switchIsPublic);
        mSwitchIsPublic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPhoto.getMetadata().setPublicFlag(isChecked);
            }
        });

        mEditTextTags = (EditText) mLayout.findViewById(R.id.editTextTags);
        mEditTextTags.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (!hasFocus) {
                    String tags = ((TextView) view).getText().toString();
                    mPhoto.getMetadata().setTags(Arrays.asList(tags.split(",")));
                }
            }
        });

        mEditTextSets = (EditText) mLayout.findViewById(R.id.editTextSets);
        mEditTextSets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPhotosets != null) {
                    new SetChooserDialog(photosetsToMap(mPhotosets)).show(
                            mActivity.getFragmentManager(), "SetChooserDialog");
                } else {
                    mActivity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
                    new LoadPhotosetsTask(new Events.IPhotosetsReadyListener() {
                        @Override
                        public void onPhotosetsReady(Photosets photosets, Exception e) {
                            mActivity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
                            if (photosets != null) {
                                mPhotosets = photosets;
                                new SetChooserDialog(photosetsToMap(mPhotosets)).show(
                                        mActivity.getFragmentManager(), "SetChooserDialog");
                            } else {
                                // TODO: alert user
                            }
                        }
                    }, mOAuth.getUser()).execute(mOAuth);
                }
            }
        });

        return mLayout;
    }

    private Map<String, Photoset> photosetsToMap(Photosets photosets) {
        HashMap<String, Photoset> ret = new HashMap();
        for (Photoset p : photosets.getPhotosets()) {
            ret.put(p.getTitle(), p);
        }
        return ret;
    }

    public void setPhoto(LocalPhotosGridFragment.LocalPhoto photo) {
        if (mPhoto != null) {
            updateMetadataFromUI();
        }
        mPhoto = photo;
        updateUIFromMetadata();
    }

    /** Store UI metadata updates to the current photo */
    public void updateMetadataFromUI() {
        mPhoto.getMetadata().setTitle(mEditTextTitle.getText().toString());
        mPhoto.getMetadata().setDescription(mEditTextDescription.getText().toString());
        mPhoto.getMetadata().setPublicFlag(mSwitchIsPublic.isChecked());

        String strTags = mEditTextTags.getText().toString();
        mPhoto.getMetadata().setTags(Arrays.asList(strTags.split(",")));
    }

    /** Update UI metadata display from the current photo */
    public void updateUIFromMetadata() {
        mEditTextTitle.setText(mPhoto.getMetadata().getTitle());
        mEditTextDescription.setText(mPhoto.getMetadata().getDescription());
        mSwitchIsPublic.setChecked(mPhoto.getMetadata().isPublicFlag());

        if (mPhoto.getMetadata().getTags() != null) {
            List<String> tags = new ArrayList<String>(mPhoto.getMetadata().getTags());
            mEditTextTags.setText(tagsToString(tags));
        } else {
            mEditTextTags.setText("");
        }
    }

    private String tagsToString(List<String> tags) {
        StringBuilder tagDisplay = new StringBuilder();
        for (int i=0; i < tags.size(); i++) {
            tagDisplay.append(tags.get(i));
        }
        return tagDisplay.substring(0, tagDisplay.length()-2);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        /* Override so as not to display the usual PhotoGridFragment options */
    }

    /* android-styled-dialogs doesn't support multichoice (yet) */
    public class SetChooserDialog extends DialogFragment {

        private List<Integer> mSelectedItems;
        private String[] mEntries;
        private HashMap<String, Photoset> mAllAvailablePhotosets;

        public SetChooserDialog(Map<String, Photoset> allAvailablePhotosets) {
            mAllAvailablePhotosets = (HashMap) allAvailablePhotosets;
            mEntries = mAllAvailablePhotosets.keySet().toArray(
                    new String[mAllAvailablePhotosets.size()]);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            mSelectedItems = new ArrayList();
            boolean[] checkedItems = null;
            if (mPhoto.getPhotosets() != null) {
                checkedItems = new boolean[mEntries.length];
                Arrays.fill(checkedItems, false);
                HashMap<String, Photoset> current = (HashMap) photosetsToMap(mPhoto.getPhotosets());
                Set<String> currentEntries = current.keySet();
                List<String> entriesList = Arrays.asList(mEntries);
                for (int i=0; i<entriesList.size(); i++) {
                    if (currentEntries.contains(entriesList.get(i))) {
                        checkedItems[i] = true;
                        mSelectedItems.add(i);
                    }
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(mActivity.getString(R.string.add_to_sets))
                    .setMultiChoiceItems(mEntries, checkedItems,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which,
                                        boolean isChecked) {
                                    if (isChecked) {
                                        mSelectedItems.add(which);
                                    } else if (mSelectedItems.contains(which)) {
                                        mSelectedItems.remove(Integer.valueOf(which));
                                    }
                                }
                            })
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            List<Photoset> selectedPhotosets = new ArrayList();
                            StringBuilder setNames = new StringBuilder();
                            for (Integer i : mSelectedItems) {
                                if (Constants.DEBUG) Log.d(TAG, mEntries[i]);
                                Photoset photoset = mAllAvailablePhotosets.get(mEntries[i]);
                                setNames.append(photoset.getTitle());
                                setNames.append(", ");
                                selectedPhotosets.add(photoset);
                            }
                            Photosets photosets = new Photosets();
                            photosets.setPhotosets(selectedPhotosets);
                            mPhoto.setPhotosets(photosets);
                            mEditTextSets.setText(setNames.substring(0, setNames.length()-2));
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dismiss();
                                }
                            });
            return builder.create();
        }
    }
}

