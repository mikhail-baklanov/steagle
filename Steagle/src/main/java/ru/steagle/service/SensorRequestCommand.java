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
import ru.steagle.datamodel.Sensor;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.GetSensorsCommand;
import ru.steagle.protocol.responce.Sensors;


public class SensorRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    public static final String PARAM_SENSOR_ID = "sensorId";

	private static final long MAX_LOADING_INTERVAL_MS = 60000;
	private static final long LOADING_PAUSE_MS = 5000;
    private static final String TAG = SensorRequestCommand.class.getName();
    protected DataModel dataModel;
	protected long time2finish;
	protected IBroadcastSender broadcastSender;
	protected String deviceId;
    protected String sensorId;
	protected String statusId;
	protected boolean isTerminated;
    protected Context context;

	public SensorRequestCommand(Context context, DataModel dataModel, IBroadcastSender broadcastSender, String deviceId, String sensorId, String statusId) {
        this.context = context;
		this.dataModel = dataModel;
		this.broadcastSender = broadcastSender;
        this.deviceId = deviceId;
		this.sensorId = sensorId;
		this.statusId = statusId;
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
		runLoading(new AsyncRun<List<Sensor>>() {
			
			@Override
			public void onSuccess(List<Sensor> list) {
				dataModel.setSensors(list);
				if (System.currentTimeMillis() > time2finish || sensorStatusChanged(list)) {
                    Intent intent = new Intent(SteagleService.BROADCAST_ACTION);
                    intent.putExtra(SteagleService.OBJECT_NAME, SteagleService.Dictionary.SENSOR_STATUS_CHANGE.toString());
                    intent.putExtra(PARAM_SENSOR_ID, sensorId);
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
	private boolean sensorStatusChanged(List<Sensor> list) {
		if (sensorId == null || statusId == null)
			return true;
		for (Sensor sensor: list) {
			if (sensorId.equals(sensor.getId()) && statusId.equals(sensor.getStatusId()))
				return true;
		}
		return false;
	}

	
	public void runLoading(final AsyncRun<List<Sensor>> asyncRun) {
        Request request = new Request().add(new GetSensorsCommand(context, deviceId));
        Log.d(SteagleService.TAG, "wait for SENSOR list changes request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(context)) {
            @Override
            public void onPostExecute(String result) {
                Log.d(SteagleService.TAG, "wait for SENSOR list changes response: " + result);
                Sensors objects = new Sensors(result);
                if (objects.isOk()) {
                    asyncRun.onSuccess(objects.getList());
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
