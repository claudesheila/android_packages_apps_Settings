package com.android.settings.jellybam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class About extends SettingsPreferenceFragment {

    public static final String TAG = "About";

    Preference mSiteUrl;
    Preference mForumUrl;
    Preference mSourceUrl;
    Preference mFacebookUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.about_rom);
        mSiteUrl = findPreference("jellybam_website");
        mSourceUrl = findPreference("jellybam_source");
        mFacebookUrl = findPreference("jellybam_facebook");

        PreferenceGroup devsGroup = (PreferenceGroup) findPreference("devs");
        ArrayList<Preference> devs = new ArrayList<Preference>();
        for (int i = 0; i < devsGroup.getPreferenceCount(); i++) {
            devs.add(devsGroup.getPreference(i));
        }
        devsGroup.removeAll();
        devsGroup.setOrderingAsAdded(false);
        Collections.shuffle(devs);
        for(int i = 0; i < devs.size(); i++) {
            Preference p = devs.get(i);
            p.setOrder(i);

            devsGroup.addPreference(p);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mSiteUrl) {
            launchUrl("http://www.bam-android.com");
        } else if (preference == mSourceUrl) {
            launchUrl("http://github.com/iGio90");
        } else if (preference == mFacebookUrl) {
            launchUrl("http://facebook.com/JellyBam");
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void launchUrl(String url) {
        Uri uriUrl = Uri.parse(url);
        Intent donate = new Intent(Intent.ACTION_VIEW, uriUrl);
        getActivity().startActivity(donate);
    }
}
