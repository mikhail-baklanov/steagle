package ru.steagle.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ru.steagle.config.Config;
import ru.steagle.config.Keys;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Event;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.request.GetEventsCommand;
import ru.steagle.protocol.responce.Events;
import ru.steagle.utils.Utils;


public class HistoryRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    private static final long MAX_LOADING_INTERVAL_MS = 20000;
    private static final long LOADING_PAUSE_MS = 4000;
    private static final String TAG = HistoryRequestCommand.class.getName();
    protected DataModel dataModel;
    protected IBroadcastSender broadcastSender;
    protected long time2finish;
    protected Date startDate;
    protected Date endDate;
    protected boolean isTerminated;
    protected Context context;
    private String level;
    private int requestId;

    public HistoryRequestCommand(Context context, DataModel dataModel, IBroadcastSender broadcastSender,
                                 Date startDate, Date endDate, String level, int requestId) {
        this.context = context;
        this.dataModel = dataModel;
        this.broadcastSender = broadcastSender;
        this.startDate = startDate;
        this.endDate = endDate;
        this.level = level;
        this.requestId = requestId;
        time2finish = System.currentTimeMillis() + MAX_LOADING_INTERVAL_MS;
    }

    protected boolean authCheck() {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        return prefs.getString(Keys.LOGIN.getPrefKey(), null) != null && prefs.getString(Keys.PASSWORD.getPrefKey(), null) != null;
    }

    @Override
    public boolean canRun() {
        return canRun && authCheck() && (System.currentTimeMillis() > time2load);
    }

    @Override
    public boolean isTerminated() {
        return isTerminated;
    }

    @Override
    public void run() {
        canRun = false;
        runLoading(new AsyncRun<List<Event>>() {

            @Override
            public void onSuccess(List<Event> list) {
                if (dataModel.getHistoryRequestId() == requestId) {
                    dataModel.setHistoryEvents(list);
                    isTerminated = true;
                    Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                    intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.HISTORY.toString());
                    intent.putExtra(SteagleService.HISTORY_REQUEST_ID, requestId);
                    broadcastSender.sendBroadcast(intent);
                }
            }

            @Override
            public void onFailure() {
                if (System.currentTimeMillis() > time2finish) {
                    isTerminated = true;
                    if (dataModel.getHistoryRequestId() == requestId) {
                        dataModel.setHistoryEvents(null);
                        Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                        intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.HISTORY.toString());
                        intent.putExtra(SteagleService.HISTORY_REQUEST_ID, requestId);
                        broadcastSender.sendBroadcast(intent);
                    }
                } else {
                    time2load = System.currentTimeMillis() + LOADING_PAUSE_MS;
                    canRun = true;
                }
            }
        });
    }

    public void runLoading(final AsyncRun<List<Event>> asyncRun) {
        Request request = new Request().add(new GetEventsCommand(context, startDate, endDate, level));
        Log.d(TAG, "GetEvents request: " + request);
        Utils.writeLogMessage("\n\nGetEvents request: " + request, true);
        ServiceTask requestTask = new ServiceTask(Config.getRegServer(context));
        String result = requestTask.execute(request.serialize());
        Log.d(TAG, "GetEvents response: " + result);
        Utils.writeLogMessage("\n\nGetEvents response: " + result, true);
        Events events = new Events(result);
        if (events.isOk()) {
            asyncRun.onSuccess(events.getList());
        } else {
            asyncRun.onFailure();
        }

    }

}
