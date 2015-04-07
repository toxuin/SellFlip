package ru.toxuin.sellflip.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.support.v4.preference.PreferenceFragment;
import android.view.Window;

import com.facebook.Session;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.johnpersano.supertoasts.util.Style;
import com.google.android.gms.common.api.Api;

import ru.toxuin.sellflip.BaseActivity;
import ru.toxuin.sellflip.BuildConfig;
import ru.toxuin.sellflip.R;
import ru.toxuin.sellflip.library.BitmapCache;
import ru.toxuin.sellflip.restapi.ApiHeaders;
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
        Preference contact = findPreference("pref_key_contact");
        Preference saveSearch = findPreference("pref_key_save_search");
        Preference version = findPreference("pref_key_version");

        cacheReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SellFlipSpiceService.clearCache();
                                BitmapCache.getInstance().clear();
                                SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
                                pref.edit().clear().commit();
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
                if(Session.getActiveSession().isOpened() && ApiHeaders.getAccessToken()!= null){
                    Session.getActiveSession().closeAndClearTokenInformation();
                    ApiHeaders.clearToken();
                    SuperToast superToast = new SuperToast(getActivity(), Style.getStyle(Style.BLUE, SuperToast.Animations.POPUP));
                    superToast.setDuration(SuperToast.Duration.SHORT);
                    superToast.setText("Successfully logged out");
                    superToast.setIcon(SuperToast.Icon.Dark.INFO, SuperToast.IconPosition.LEFT);
                    superToast.show();
                } else {
                  BaseActivity.showLogInDialog();
                }

                return true;
            }
        });

        privacy.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BaseActivity.setContent(new PrivacyPolicyFragment());
                BaseActivity.setContentTitle("Privacy Policy");
                return true;
            }
        });

        columns.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!(newValue instanceof String)) return false;
                String newString = (String) newValue;
                SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(preference.getKey(), newString);
                editor.apply();
                return true;
            }
        });

        contact.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override public boolean onPreferenceClick(Preference preference) {
                BaseActivity.setContent(new SendFeedbackFragment());
                BaseActivity.setContentTitle("Contact Us");
                return true;
            }
        });

        saveSearch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (!(newValue instanceof Boolean)) return false;
                Boolean boolValue = (Boolean) newValue;
                SharedPreferences pref = getActivity().getSharedPreferences(getActivity().getString(R.string.app_preference_key), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean(preference.getKey(), boolValue);
                editor.apply();
                return true;
            }
        });

        version.setTitle("App Version #: " + BuildConfig.VERSION_CODE);
        version.setSummary("App Codename: " + BuildConfig.VERSION_NAME);


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
