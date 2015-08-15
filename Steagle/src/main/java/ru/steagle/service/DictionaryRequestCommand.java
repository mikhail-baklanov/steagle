package ru.steagle.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.steagle.config.Keys;
import ru.steagle.datamodel.DataModel;

public abstract class DictionaryRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    public static final long LOADING_PAUSE_MS = 60000;
    private static final String TAG = DictionaryRequestCommand.class.getName();
    protected DataModel dataModel;
    protected IBroadcastSender broadcastSender;
    protected SteagleService.Dictionary dictionary;
    protected Context context;

    public DictionaryRequestCommand(SteagleService.Dictionary dictionary, DataModel dataModel, IBroadcastSender broadcastSender) {
        this.dictionary = dictionary;
        this.dataModel = dataModel;
        this.broadcastSender = broadcastSender;
    }

    public DictionaryRequestCommand(Context context, SteagleService.Dictionary dictionary, DataModel dataModel, IBroadcastSender broadcastSender) {
        this.dictionary = dictionary;
        this.dataModel = dataModel;
        this.broadcastSender = broadcastSender;
        this.context = context;
    }

    protected boolean authCheck() {
        if (context != null) {
            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            return prefs.getString(Keys.LOGIN.getPrefKey(), null) != null && prefs.getString(Keys.PASSWORD.getPrefKey(), null) != null;
        } else
            return true;
    }

    @Override
    public boolean canRun() {
        return canRun && authCheck() && (System.currentTimeMillis() > time2load);
    }

    @Override
    public void run() {
        canRun = false;
        runLoading(dataModel, new AsyncRun<Void>() {

            @Override
            public void onSuccess(Void data) {
                notifyObjectChanges();
                time2load = System.currentTimeMillis() + LOADING_PAUSE_MS;
            }

            @Override
            public void onFailure() {
                time2load = System.currentTimeMillis() + LOADING_PAUSE_MS;
                canRun = true;
            }
        });
    }

    private void notifyObjectChanges() {
        Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
        Log.d(SteagleService.TAG, "Service: notify about " + dictionary + " list changes");
        intent.putExtra(SteagleService.OBJECT_NAME, dictionary.toString());
        broadcastSender.sendBroadcast(intent);
    }

    abstract public void runLoading(final DataModel dataModel, final AsyncRun asyncRun);

    public void reset(boolean force) {
        canRun = true;
        if (force)
            time2load = 0;
    }
}
