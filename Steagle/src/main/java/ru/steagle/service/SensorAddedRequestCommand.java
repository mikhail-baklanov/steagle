package ru.steagle.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Device;
import ru.steagle.datamodel.Sensor;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.Command;
import ru.steagle.protocol.request.GetSensorsCommand;
import ru.steagle.protocol.responce.Sensors;


public class SensorAddedRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    private static final long MAX_LOADING_INTERVAL_MS = 60000;
    private static final long LOADING_PAUSE_MS = 3000;
    private static final String TAG = SensorAddedRequestCommand.class.getName();
    protected DataModel dataModel;
    protected long time2finish;
    protected IBroadcastSender broadcastSender;
    protected boolean isTerminated;
    protected Context context;

    public SensorAddedRequestCommand(Context context, DataModel dataModel, IBroadcastSender broadcastSender) {
        this.context = context;
        this.dataModel = dataModel;
        this.broadcastSender = broadcastSender;
        time2finish = System.currentTimeMillis() + MAX_LOADING_INTERVAL_MS;
    }

    @Override
    public boolean canRun() {
        return canRun && (System.currentTimeMillis() > time2load);
    }

    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public void run() {
        canRun = false;
        runLoading(new AsyncRun<List<Sensor>>() {

            @Override
            public void onSuccess(List<Sensor> list) {
                dataModel.setSensors(list);
                if (System.currentTimeMillis() > time2finish) {
                    Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                    intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.SENSOR.toString());
                    broadcastSender.sendBroadcast(intent);
                    isTerminated = true;
                } else {
                    Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                    intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.SENSOR.toString());
                    broadcastSender.sendBroadcast(intent);
                }
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

    public void runLoading(final AsyncRun<List<Sensor>> asyncRun) {
        if (dataModel.getDevices() == null || dataModel.getDevices().size() == 0)
            asyncRun.onFailure();
        List<Command> commandList = new ArrayList<>();
        for (Device d : dataModel.getDevices())
            commandList.add(new GetSensorsCommand(context, d.getId()));
        Request request = new Request();
        for (Command c : commandList)
            request.add(c);
        final SteagleService.Dictionary dictionary = SteagleService.Dictionary.SENSOR;
        Log.d(SteagleService.TAG, "get " + dictionary + " list request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(context)) {
            @Override
            public void onPostExecute(String result) {
                Log.d(SteagleService.TAG, "get " + dictionary + " list response: " + result);
                Sensors objects = new Sensors(result);
                if (objects.isOk()) {
                    asyncRun.onSuccess(objects.getList());

                    Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                    intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.DEVICE.toString());
                    broadcastSender.sendBroadcast(intent);
                } else {
                    asyncRun.onFailure();
                }
            }

            @Override
            protected void onCancelled() {
                asyncRun.onFailure();
            }

        };
        requestTask.execute(request.serialize());
    }


}
