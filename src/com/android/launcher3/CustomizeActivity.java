/*
 * Copyright (C) 2017 Paranoid Android
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

package com.android.launcher3;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class CustomizeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new CustomizationFragment())
                .commit();
    }

    public static class CustomizationFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        private String mDefaultIconPack;

        private IconsHandler mIconsHandler;
        private PackageManager mPackageManager;
        private Preference mIconPack;
        private Preference mHiddenApp;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.launcher_customization);

            mPackageManager = getActivity().getPackageManager();

            mDefaultIconPack = getString(R.string.default_iconpack);
            mIconsHandler = IconCache.getIconsHandler(getActivity().getApplicationContext());
            mIconPack = (Preference) findPreference(Utilities.KEY_ICON_PACK);
            mHiddenApp = (Preference) findPreference(Utilities.KEY_HIDDEN_APPS);

            reloadIconPackSummary();
        }

        @Override
        public void onResume() {
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .registerOnSharedPreferenceChangeListener(this);
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
            PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .unregisterOnSharedPreferenceChangeListener(this);
            mIconsHandler.hideDialog();
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference pref) {
            if (pref == mIconPack) {
                mIconsHandler.showDialog(getActivity());
                return true;
            }
            if (pref == mHiddenApp) {
                Intent intent = new Intent(getActivity(), MultiSelectRecyclerViewActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (Utilities.KEY_ROUND_ICONS.equals(key)) {
                mIconsHandler.switchIconPacks(mDefaultIconPack);
            }
            if (Utilities.KEY_HOTSEAT.equals(key)) {
                Launcher.setUpdateHotseatColor();
            }
            reloadIconPackSummary();
        }

        private void reloadIconPackSummary() {
            ApplicationInfo info = null;
            String iconPack = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(Utilities.KEY_ICON_PACK, mDefaultIconPack);

            String packageLabel = getActivity().getString(R.string.default_iconpack_title);
            Drawable packageIcon = getActivity().getDrawable(R.drawable.icon_pack);
            if (!mIconsHandler.isDefaultIconPack()) {
                try {
                    info = mPackageManager.getApplicationInfo(iconPack, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (info != null) {
                    packageLabel = mPackageManager.getApplicationLabel(info).toString();
                    packageIcon = mPackageManager.getApplicationIcon(info);
                }
            }
            mIconPack.setSummary(packageLabel);
            mIconPack.setIcon(packageIcon);
            manageRoundIconsPref();
        }

        private void manageRoundIconsPref() {

            Preference roundIcons = (Preference) findPreference(Utilities.KEY_ROUND_ICONS);
            roundIcons.setEnabled(mIconsHandler.isDefaultIconPack());
        }
    }
}
