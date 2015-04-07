package ru.steagle.utils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.service.SteagleService;

public class Utils {
    private static final String TAG = Utils.class.toString();

    /**
     * Получение номера формы существительного для заданного количества.
     *
     * @param number количество
     * @return номер формы: 0 - попугай, 1 - попугая, 2 - попугаев
     */
    public static int getDeclinationIndex(int number) {
        number = number % 100;
        if (number >= 11 && number <= 19) {
            return 2;
        } else {
            number = number % 10;
            switch (number) {
                case 1:
                    return 0;
                case 2:
                case 3:
                case 4:
                    return 1;
                default:
                    return 2;
            }
        }
    }

    public static boolean isObjectInSet(String object, Set<SteagleService.Dictionary> set) {
        for (SteagleService.Dictionary d : set)
            if (d.toString().equals(object))
                return true;
        return false;
    }

    public static Date getDay(Date d) {
        Calendar c = GregorianCalendar.getInstance();
        c.setTime(d);
        Calendar day = GregorianCalendar.getInstance();
        day.clear();
        day.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        return day.getTime();
    }

    public static void showConfirmDialog(Context context, String title, String message, String yesButtonText, String noButtonText, final Runnable onYesClick) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(yesButtonText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onYesClick.run();
                    }
                })
                .setNegativeButton(noButtonText, null);
        if (title != null)
            builder.setTitle(title);
        builder.show();
    }

    public static ProgressDialog getProgressDialog(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(context.getResources().getString(R.string.updating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    public static void showServiceConnectionError(Context context) {
        Toast.makeText(context, R.string.service_connection_error, Toast.LENGTH_LONG).show();
    }

    public static void showNetworkError(Context context) {
        Toast.makeText(context, R.string.network_error, Toast.LENGTH_LONG).show();
    }


    public static boolean detectWiFi(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi != null && mWifi.isConnected();
    }

    public static boolean detect3G(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mMobile = connManager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        return mMobile != null && mMobile.isConnected();
    }

    public static boolean inRoaming(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager != null && telephonyManager.isNetworkRoaming();
    }

    public static boolean isNetworkAvailable(Context context) {
        boolean isWifiOn = Utils.detectWiFi(context);
//        Log.d(TAG, "Wifi is " + (isWifiOn ? "ON" : "OFF"));
        if (isWifiOn)
            return true;
        else if (Config.isWifiOnly(context)) {
//            Log.d(TAG, "can use only WifiOnly -> no network is available");
            return false;
        } else {
//            Log.d(TAG, "can use 3G");
            boolean is3GOn = Utils.detect3G(context);
//            Log.d(TAG, "3G is " + (is3GOn ? "ON" : "OFF"));
            return is3GOn;
        }
    }

    public static void vibrate(Context context) {
        Vibrator v = (Vibrator) context
                .getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null)
            v.vibrate(50);
    }

}
