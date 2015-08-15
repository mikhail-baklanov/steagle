package ru.steagle.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.steagle.config.Keys;

/**
 * Created by bmw on 08.03.14.
 */
public class BootReceiver extends BroadcastReceiver {

    private final static String TAG = BootReceiver.class.getName();

    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive " + intent.getAction());

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean autoLoad = prefs.getBoolean(Keys.AUTO_LOAD.getPrefKey(), false);

        if (autoLoad) {
            Intent i = new Intent(context, SteagleService.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startService(i);
        }
    }
}
