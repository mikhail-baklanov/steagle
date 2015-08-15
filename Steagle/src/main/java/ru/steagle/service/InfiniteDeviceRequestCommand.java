package ru.steagle.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.List;

import ru.steagle.config.Config;
import ru.steagle.config.Keys;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Device;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.request.GetDevicesCommand;
import ru.steagle.protocol.responce.Devices;


public class InfiniteDeviceRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    private static final long LOADING_PAUSE_MS = 15000;
    private static final String TAG = InfiniteDeviceRequestCommand.class.getName();
    protected DataModel dataModel;
    protected IBroadcastSender broadcastSender;
    protected Context context;
    protected boolean isActive;

    public InfiniteDeviceRequestCommand(Context context, DataModel dataModel, IBroadcastSender broadcastSender) {
        this.context = context;
        this.dataModel = dataModel;
        this.broadcastSender = broadcastSender;
    }

    protected boolean authCheck() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(Keys.LOGIN.getPrefKey(), null) != null && prefs.getString(Keys.PASSWORD.getPrefKey(), null) != null;
    }

    @Override
    public boolean canRun() {
        return isActive && canRun && authCheck() && (System.currentTimeMillis() > time2load);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public void run() {
        canRun = false;
        runLoading(new AsyncRun<List<Device>>() {

            @Override
            public void onSuccess(List<Device> list) {
                dataModel.setDevices(list);
                Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.DEVICE.toString());
                broadcastSender.sendBroadcast(intent);

                time2load = System.currentTimeMillis() + LOADING_PAUSE_MS;
                canRun = true;
            }

            @Override
            public void onFailure() {
                time2load = System.currentTimeMillis() + LOADING_PAUSE_MS;
                canRun = true;
            }
        });
    }


    public void runLoading(final AsyncRun<List<Device>> asyncRun) {
        Request request = new Request().add(new GetDevicesCommand(context));
        Log.d(SteagleService.TAG, "wait for DEVICE list changes request: " + request);
        ServiceTask requestTask = new ServiceTask(Config.getRegServer(context));
        String result = requestTask.execute(request.serialize());
        Log.d(SteagleService.TAG, "wait for DEVICE list changes response: " + result);
        Devices objects = new Devices(result);
        if (objects.isOk()) {
            asyncRun.onSuccess(objects.getList());
        } else {
            asyncRun.onFailure();
        }
    }

}
