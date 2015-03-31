package ru.toxuin.sellflip.fragments;

import android.os.Bundle;
import android.support.v4.preference.PreferenceFragment;

import ru.toxuin.sellflip.R;

public class PrefsFragment extends PreferenceFragment {
    @Override public void onCreate(Bundle savedInstanceState) {


//
//        String settings = getArguments().getString("settings");
//
//        if ("perf1".equals(settings)) {
//            addPreferencesFromResource(R.xml.preferences);
//        } else if ("perf2".equals(settings)) {
//            addPreferencesFromResource(R.xml.preferences);
//        }

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
