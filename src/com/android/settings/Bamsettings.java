/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.content.Intent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.IWindowManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class Bamsettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "Bamsettings";

    private static final String PREF_POWER_CRT_SCREEN_ON = "system_power_crt_screen_on";
    private static final String PREF_POWER_CRT_SCREEN_OFF = "system_power_crt_screen_off";
    
    private CheckBoxPreference mCrtOff;
    private CheckBoxPreference mCrtOn;
    private final Configuration mCurConfig = new Configuration();
    private Context mContext;

    private boolean isCrtOffChecked = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getContentResolver();
        mContext = getActivity();

        addPreferencesFromResource(R.xml.jellybam_settings);
        PreferenceScreen prefs = getPreferenceScreen();

       // respect device default configuration
        // true fades while false animates
        boolean electronBeamFadesConfig = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_animateScreenLights);

        // use this to enable/disable crt on feature
        // crt only works if crt off is enabled
        // total system failure if only crt on is enabled
        isCrtOffChecked = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                electronBeamFadesConfig ? 0 : 1) == 1;

        mCrtOff = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_OFF);
        mCrtOff.setChecked(isCrtOffChecked);
        mCrtOff.setOnPreferenceChangeListener(this);

        mCrtOn = (CheckBoxPreference) findPreference(PREF_POWER_CRT_SCREEN_ON);
        mCrtOn.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0) == 1);
        mCrtOn.setEnabled(isCrtOffChecked);
        mCrtOn.setOnPreferenceChangeListener(this);

    }
    
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
         if (mCrtOff.equals(preference)) {
            isCrtOffChecked = ((Boolean) newValue).booleanValue();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_POWER_ENABLE_CRT_OFF,
                    (isCrtOffChecked ? 1 : 0));
            // if crt off gets turned off, crt on gets turned off and disabled
            if (!isCrtOffChecked) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.SYSTEM_POWER_ENABLE_CRT_ON, 0);
                mCrtOn.setChecked(false);
            }
            mCrtOn.setEnabled(isCrtOffChecked);
            return true;
        } else if (mCrtOn.equals(preference)) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.SYSTEM_POWER_ENABLE_CRT_ON,
                    ((Boolean) newValue).booleanValue() ? 1 : 0);
            return true;
        }
        return false;
    }

    private boolean removePreferenceIfPackageNotInstalled(Preference preference) {
        String intentUri = ((PreferenceScreen) preference).getIntent().toUri(1);
        Pattern pattern = Pattern.compile("component=([^/]+)/");
        Matcher matcher = pattern.matcher(intentUri);

        String packageName = matcher.find() ? matcher.group(1) : null;
        if (packageName != null) {
            try {
                getPackageManager().getPackageInfo(packageName, 0);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "package " + packageName + " not installed, hiding preference.");
                getPreferenceScreen().removePreference(preference);
                return true;
            }
        }
        return false;
    }

}


