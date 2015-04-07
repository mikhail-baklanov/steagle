package ru.steagle.service;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by bmw on 15.02.14.
 */
public class SteagleServiceConnector {
    private ServiceConnection serviceConnection;
    private SteagleService.LocalBinder serviceBinder;
    private String tag;
    private Context context;

    public SteagleServiceConnector(String loggerTag) {
        this.tag = loggerTag;
    }

    public void bind(Context context, final Runnable onConnected) {
        if (this.context != null)
            return;
        this.context = context;
        Intent serviceIntent = new Intent(context, SteagleService.class);

        serviceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                Log.d(tag, "onServiceConnected");
                serviceBinder = (SteagleService.LocalBinder) binder;
                if (onConnected != null)
                    onConnected.run();
            }

            public void onServiceDisconnected(ComponentName name) {
                Log.d(tag, "onServiceDisconnected");
                serviceBinder = null;
            }
        };
        Log.d(tag, "bind service");
        context.bindService(serviceIntent, serviceConnection, Activity.BIND_AUTO_CREATE);
    }

    public void unbind() {
        if (context == null)
            return;
        Log.d(tag, "unbind service");
        context.unbindService(serviceConnection);
        context = null;
    }

    public SteagleService.LocalBinder getServiceBinder() {
        return serviceBinder;
    }
}
