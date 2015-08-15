package ru.steagle.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import ru.steagle.R;
import ru.steagle.config.Keys;
import ru.steagle.service.SteagleService;

/**
 * Created by bmw on 08.02.14.
 */
public class SettingsFragment extends PreferenceFragment {

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
        prefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (Keys.AUTO_LOAD.getPrefKey().equals(key)) {
                    boolean autoLoad = prefs.getBoolean(Keys.AUTO_LOAD.getPrefKey(), false);
                    Intent i = new Intent(getActivity(), SteagleService.class);
                    if (autoLoad) {
                        getActivity().startService(i);
                    } else {
                        getActivity().stopService(i);
                    }

                }
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onDestroy() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }
}
