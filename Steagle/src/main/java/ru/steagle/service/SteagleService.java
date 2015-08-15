package ru.steagle.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Device;
import ru.steagle.datamodel.Event;
import ru.steagle.datamodel.Sensor;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.request.Command;
import ru.steagle.protocol.request.GetCurrenciesCommand;
import ru.steagle.protocol.request.GetDevModeSrcsCommand;
import ru.steagle.protocol.request.GetDevModesCommand;
import ru.steagle.protocol.request.GetDeviceStatesCommand;
import ru.steagle.protocol.request.GetDeviceStatusesCommand;
import ru.steagle.protocol.request.GetDevicesCommand;
import ru.steagle.protocol.request.GetLevelsCommand;
import ru.steagle.protocol.request.GetSensorStatusesCommand;
import ru.steagle.protocol.request.GetSensorTypesCommand;
import ru.steagle.protocol.request.GetSensorsCommand;
import ru.steagle.protocol.request.GetTarifsCommand;
import ru.steagle.protocol.request.GetTimeZonesCommand;
import ru.steagle.protocol.request.GetUserStatusesCommand;
import ru.steagle.protocol.responce.Currencies;
import ru.steagle.protocol.responce.DevModeSrcs;
import ru.steagle.protocol.responce.DevModes;
import ru.steagle.protocol.responce.DeviceStates;
import ru.steagle.protocol.responce.DeviceStatuses;
import ru.steagle.protocol.responce.Devices;
import ru.steagle.protocol.responce.Levels;
import ru.steagle.protocol.responce.SensorStatuses;
import ru.steagle.protocol.responce.SensorTypes;
import ru.steagle.protocol.responce.Sensors;
import ru.steagle.protocol.responce.Tarifs;
import ru.steagle.protocol.responce.TimeZones;
import ru.steagle.protocol.responce.UserStatuses;
import ru.steagle.utils.Utils;

public class SteagleService extends Service {
    public static final String BROADCAST_ACTION = SteagleService.class.getCanonicalName();
    public static final String OBJECT_NAME = "objectName";
    public static final String TAG = SteagleService.class.getName();
    public static final String HISTORY_REQUEST_ID = "historyRequestId";

    private List<IRequestDataCommand> requestDataCommands = new ArrayList<>();

    public enum Dictionary {
        USER_STATUS, CURRENCY, LEVEL, TIME_ZONE, TARIF, DEVICE_STATUS,
        DEV_MODE_SRC, DEV_MODE, DEVICE, SENSOR, SENSOR_STATUS, DEVICE_STATUS_CHANGE,
        SENSOR_STATUS_CHANGE, DEVICE_STATE, SENSOR_TYPE, HISTORY
    }

    private DataModel dm = new DataModel();
    private final IBinder binder = new LocalBinder();
    private Thread mainThread;

    private IBroadcastSender broadcastSender = new IBroadcastSender() {
        @Override
        public void sendBroadcast(Intent intent) {
            SteagleService.this.sendBroadcast(intent);
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Received start id " + startId + ": " + intent);
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "Service onCreate");
        startMainLoop();
    }

    private void startMainLoop() {
        Log.d(TAG, "start main loop");
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                requestDataCommands.clear();
                initRequestDataCommands();
                mainLoop();
            }
        });
        mainThread.start();
    }

    private void initRequestDataCommands() {

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.USER_STATUS, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetUserStatusesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        UserStatuses objects = new UserStatuses(result);
                        if (objects.isOk()) {
                            dm.setUserStatuses(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });

            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.CURRENCY, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetCurrenciesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        Currencies objects = new Currencies(result);
                        if (objects.isOk()) {
                            dm.setCurrencies(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.TIME_ZONE, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetTimeZonesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        TimeZones objects = new TimeZones(result);
                        if (objects.isOk()) {
                            dm.setTimeZones(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.TARIF, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetTarifsCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        Tarifs objects = new Tarifs(result);
                        if (objects.isOk()) {
                            dm.setTarifs(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.DEV_MODE, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetDevModesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        DevModes objects = new DevModes(result);
                        if (objects.isOk()) {
                            dm.setDevModes(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.DEV_MODE_SRC, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetDevModeSrcsCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        DevModeSrcs objects = new DevModeSrcs(result);
                        if (objects.isOk()) {
                            dm.setDevModeSrcs(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.LEVEL, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetLevelsCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        Levels objects = new Levels(result);
                        if (objects.isOk()) {
                            dm.setLevels(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.DEVICE_STATUS, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetDeviceStatusesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        DeviceStatuses objects = new DeviceStatuses(result);
                        if (objects.isOk()) {
                            dm.setDeviceStatuses(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.DEVICE_STATE, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetDeviceStatesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        DeviceStates objects = new DeviceStates(result);
                        if (objects.isOk()) {
                            dm.setDeviceStates(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.SENSOR_STATUS, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetSensorStatusesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        SensorStatuses objects = new SensorStatuses(result);
                        if (objects.isOk()) {
                            dm.setSensorStatuses(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(Dictionary.SENSOR_TYPE, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetSensorTypesCommand(), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        SensorTypes objects = new SensorTypes(result);
                        if (objects.isOk()) {
                            dm.setSensorTypes(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(this, Dictionary.DEVICE, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                loadObjects(dictionary, new GetDevicesCommand(getBaseContext()), new IResultProcessor() {
                    @Override
                    public void process(String result) {
                        Devices objects = new Devices(result);
                        if (objects.isOk()) {
                            dm.setDevices(objects.getList());
                            asyncRun.onSuccess(null);
                        } else {
                            asyncRun.onFailure();
                        }
                    }
                });
            }
        });

        requestDataCommands.add(new DictionaryRequestCommand(this, Dictionary.SENSOR, dm, broadcastSender) {
            @Override
            public void runLoading(final DataModel dataModel, final AsyncRun asyncRun) {
                if (dm.getDevices() == null || dm.getDevices().size() == 0) {
                    asyncRun.onFailure();
                    // если устойства не считаны, время ожидания изменяем на 1 сек, чтобы быстрее дождаться считанных устройств
                    time2load = System.currentTimeMillis() + 1000;
                } else {
                    List<Command> commandList = new ArrayList<>();
                    for (Device d : dm.getDevices())
                        commandList.add(new GetSensorsCommand(getBaseContext(), d.getId()));
                    loadObjects(dictionary, commandList, new IResultProcessor() {
                        @Override
                        public void process(String result) {
                            Sensors objects = new Sensors(result);
                            if (objects.isOk()) {
                                dm.setSensors(objects.getList());
                                asyncRun.onSuccess(null);
                                notifyObjectChanges(Dictionary.DEVICE);
                            } else {
                                asyncRun.onFailure();
                            }
                        }
                    });
                }
            }
        });

        requestDataCommands.add(new InfiniteDeviceRequestCommand(this, dm, broadcastSender));
        requestDataCommands.add(new NotificationRequestCommand(this));

    }

    private void mainLoop() {
        while (!mainThread.interrupted()) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (Utils.isNetworkAvailable(this)) {
                    synchronized (requestDataCommands) {
                        Iterator<IRequestDataCommand> iterator = requestDataCommands.iterator();
                        while (iterator.hasNext()) {
                            IRequestDataCommand requestDataCommand = iterator.next();
                            if (requestDataCommand.isTerminated()) {
                                iterator.remove();
                            } else {
                                if (requestDataCommand.canRun()) {
                                    requestDataCommand.run();
                                }
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private interface IResultProcessor {
        void process(String result);
    }

    private void loadObjects(final Dictionary dictionary, final Command command, final IResultProcessor resultProcessor) {

        Request request = new Request().add(command);
        doRequest(dictionary, request, resultProcessor);
    }

    private void loadObjects(final Dictionary dictionary, final List<Command> commands, final IResultProcessor resultProcessor) {

        Request request = new Request();
        for (Command c : commands)
            request.add(c);
        doRequest(dictionary, request, resultProcessor);
    }

    private void doRequest(final Dictionary dictionary, Request request, final IResultProcessor resultProcessor) {
        Log.d(TAG, "get " + dictionary + " list request: " + request);
        ServiceTask requestTask = new ServiceTask(Config.getRegServer(this));
        String result = requestTask.execute(request.serialize());
        Log.d(TAG, "get " + dictionary + " list response: " + result);
        resultProcessor.process(result);
    }

    private void notifyObjectChanges(Dictionary dictionary) {
        Intent intent = new Intent(BROADCAST_ACTION);
        Log.d(TAG, "Service: notify about " + dictionary + " list changes");
        intent.putExtra(OBJECT_NAME, dictionary.toString());
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service Destroyed");
        super.onDestroy();
    }

    public class LocalBinder extends Binder {

        public DataModel getDataModel() {
            return dm;
        }

        public void clearUserData() {
            SteagleService.this.clearUserData();
        }

        public void setDeviceName(String deviceId, String deviceName) {
            SteagleService.this.setDeviceName(deviceId, deviceName);
        }

        public void waitForDeviceStatus(String deviceId, String statusId) {
            SteagleService.this.waitForDeviceStatus(deviceId, statusId);
        }

        public void setSensorName(String sensorId, String sensorName) {
            SteagleService.this.setSensorName(sensorId, sensorName);
        }

        public void waitForSensorStatus(String deviceId, String sensorId, String statusId) {
            SteagleService.this.waitForSensorMode(deviceId, sensorId, statusId);
        }

        public void deleteSensor(String sensorId) {
            SteagleService.this.deleteSensor(sensorId);
        }

        public void waitForSensorAdded() {
            SteagleService.this.waitForSensorAdded();
        }

        public void setTimeZoneId(String timeZone) {
            dm.setTimeZoneId(timeZone);
            notifyObjectChanges(Dictionary.TIME_ZONE);
        }

        public void activateDeviceRequests() {
            SteagleService.this.activateDeviceRequests();
        }

        public void deactivateDeviceRequests() {
            SteagleService.this.deactivateDeviceRequests();
        }

        public int createHistoryRequest(Date startDate, Date endDate, String level) {
            return SteagleService.this.createHistoryRequest(startDate, endDate, level);

        }

        public List<Event> getHistoryEvents() {
            return SteagleService.this.getHistoryEvents();
        }
    }

    private List<Event> getHistoryEvents() {
        return dm.getHistoryEvents();
    }

    private int createHistoryRequest(Date startDate, Date endDate, String level) {
        int counter = dm.incHistoryRequestId();
        synchronized (requestDataCommands) {
            requestDataCommands.add(new HistoryRequestCommand(this, dm, broadcastSender, startDate, endDate, level, counter));
        }
        return counter;
    }

    private void activateDeviceRequests() {
        setDeviceRequests(true);
    }

    private void deactivateDeviceRequests() {
        setDeviceRequests(false);
    }

    private void setDeviceRequests(boolean isActive) {
        synchronized (requestDataCommands) {
            for (IRequestDataCommand cmd : requestDataCommands) {
                if (cmd instanceof InfiniteDeviceRequestCommand) {
                    InfiniteDeviceRequestCommand d = (InfiniteDeviceRequestCommand) cmd;
                    d.setActive(isActive);
                }
            }
        }
    }

    private void waitForSensorAdded() {
        synchronized (requestDataCommands) {
            requestDataCommands.add(new SensorAddedRequestCommand(this, dm, broadcastSender));
        }
    }

    private void deleteSensor(String sensorId) {
        dm.deleteSensor(sensorId);
        notifyObjectChanges(Dictionary.SENSOR);
        notifyObjectChanges(Dictionary.DEVICE);
    }

    private void waitForSensorMode(String deviceId, String sensorId, String statusId) {
        synchronized (requestDataCommands) {
            requestDataCommands.add(new SensorRequestCommand(this, dm, broadcastSender, deviceId, sensorId, statusId));
        }
    }

    private void setSensorName(String sensorId, String sensorName) {
        dm.setSensorName(sensorId, sensorName);
        notifyObjectChanges(Dictionary.SENSOR);
    }

    private void waitForDeviceStatus(String deviceId, String statusId) {
        synchronized (requestDataCommands) {

            requestDataCommands.add(new DeviceRequestCommand(this, dm, broadcastSender, deviceId, statusId));
        }
    }

    private void setDeviceName(String deviceId, String deviceName) {
        dm.setDeviceName(deviceId, deviceName);
        notifyObjectChanges(Dictionary.DEVICE);
    }

    private void clearUserData() {
        dm.setDevices(new ArrayList<Device>());
        dm.setSensors(new ArrayList<Sensor>());
        synchronized (requestDataCommands) {
            for (IRequestDataCommand cmd : requestDataCommands) {
                if (cmd instanceof DictionaryRequestCommand) {
                    DictionaryRequestCommand d = (DictionaryRequestCommand) cmd;
                    if (d.dictionary.equals(Dictionary.DEVICE) || d.dictionary.equals(Dictionary.SENSOR)) {
                        d.reset(true);
                    }
                }
            }
        }
    }

}
