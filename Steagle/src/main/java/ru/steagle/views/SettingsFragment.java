package ru.steagle.views;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.TextView;

import ru.steagle.R;

/**
 * Created by bmw on 08.02.14.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
//        ((TextView)getView().findViewById(R.id.title)).setText(getString(R.string.action_settings));
    }

}