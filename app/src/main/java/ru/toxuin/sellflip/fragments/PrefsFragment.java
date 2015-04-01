package ru.toxuin.sellflip.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;

import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.restapi.SellFlipSpiceService;

public class PrefsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        BaseActivity.setContentTitle("Settings");

        Preference cacheReset = findPreference("pref_key_clear_cache");
        Preference logout = findPreference("pref_key_logout");
        Preference columns = findPreference("pref_key_search_result_columns");
        Preference privacy = findPreference("pref_key_privacy_policy");

        cacheReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SellFlipSpiceService.clearCache();
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                return true;
            }
        });

        logout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BaseActivity.showLogInDialog();
                return true;
            }
        });

        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BaseActivity.showPrivacyDialog();
                return true;
            }
        });

        columns.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!(newValue instanceof String)) return false;
                String newString = (String) newValue;
                SharedPreferences customSharedPreference = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = customSharedPreference.edit();
                editor.putString(preference.getKey(), newString);
                editor.apply();
                return true;
            }
        });



//
//        String settings = getArguments().getString("settings");
//
//        if ("perf1".equals(settings)) {
//            addPreferencesFromResource(R.xml.preferences);
//        } else if ("perf2".equals(settings)) {
//            addPreferencesFromResource(R.xml.preferences);
//        }
    }
}
