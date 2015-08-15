package ru.steagle.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ru.steagle.R;

/**
 * Created by bmw on 07.02.14.
 */
public class Config {
    private Config() {

    }

    public static String getRegServer(Context context) {
        return context.getString(R.string.regServer);
    }

    public static String getNotifyServer(Context context) {
        return context.getString(R.string.notifyServer);
    }

    public static boolean isWifiOnly(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean useMobileInet = prefs.getBoolean(Keys.USE_MOBILE_INET.getPrefKey(), false);
        return !useMobileInet;
    }

    public static boolean isNotifySoundOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean result = prefs.getBoolean(Keys.SOUND_NOTIFY.getPrefKey(), true);
        return result;
    }

    public static boolean isNotifyVibroOn(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean result = prefs.getBoolean(Keys.VIBRO_NOTIFY.getPrefKey(), true);
        return result;
    }

    public static String getHistoryLimit(Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String historyLimit = prefs.getString(Keys.HISTORY_LIMIT.getPrefKey(),
                context.getResources().getStringArray(R.array.history_limit_values)[3]);
        return historyLimit;
    }
}
