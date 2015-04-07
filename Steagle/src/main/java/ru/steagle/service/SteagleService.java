package ru.steagle.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.util.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.protocol.request.GetDeviceStatesCommand;
import ru.steagle.protocol.request.GetSensorStatusesCommand;
import ru.steagle.protocol.responce.DeviceStates;
import ru.steagle.protocol.responce.SensorStatuses;
import ru.steagle.views.NotificationsViewActivity;
import ru.steagle.R;
import ru.steagle.utils.Utils;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.Command;
import ru.steagle.protocol.request.GetCurrenciesCommand;
import ru.steagle.protocol.request.GetDevModeSrcsCommand;
import ru.steagle.protocol.request.GetDevModesCommand;
import ru.steagle.protocol.request.GetDeviceStatusesCommand;
import ru.steagle.protocol.request.GetDevicesCommand;
import ru.steagle.protocol.request.GetLevelsCommand;
import ru.steagle.protocol.request.GetNotificationsCommand;
import ru.steagle.protocol.request.GetNotifySessionCommand;
import ru.steagle.protocol.request.GetSensorsCommand;
import ru.steagle.protocol.request.GetTarifsCommand;
import ru.steagle.protocol.request.GetTimeZonesCommand;
import ru.steagle.protocol.request.GetUserStatusesCommand;
import ru.steagle.protocol.responce.Currencies;
import ru.steagle.protocol.responce.DevModeSrcs;
import ru.steagle.protocol.responce.DevModes;
import ru.steagle.datamodel.Device;
import ru.steagle.protocol.responce.DeviceStatuses;
import ru.steagle.protocol.responce.Devices;
import ru.steagle.protocol.responce.Levels;
import ru.steagle.protocol.responce.Notifications;
import ru.steagle.protocol.responce.NotifySession;
import ru.steagle.datamodel.Sensor;
import ru.steagle.protocol.responce.Sensors;
import ru.steagle.protocol.responce.Tarifs;
import ru.steagle.protocol.responce.TimeZones;
import ru.steagle.protocol.responce.UserStatuses;

public class SteagleService extends Service {
    public static final String BROADCAST_ACTION = SteagleService.class.getCanonicalName();
    public static final String OBJECT_NAME = "objectName";

    private static final int DICTIONARY_REQUEST_PAUSE_MS = 60000;
    private static final int NOTIFICATIONS_REQUEST_PAUSE_MS = 10000;
    private static final String TAG = SteagleService.class.toString();
    private long time2loadDictionaries;
    private long time2loadNotifications;
    private String sessionId;
    private int notificationID;
    private List<WaitingObject> waitingObjects = new ArrayList<>();

    private static class WaitingObject {
        private long time2load;
        private long time2stop;
        private Dictionary object;
        public WaitingObject(Dictionary object, long time2stop) {
            this.object = object;
            this.time2stop = time2stop;
            this.time2load = new Date().getTime() + 10000;
        }
    }

    public enum Dictionary {
        USER_STATUS, CURRENCY, LEVEL, TIME_ZONE, TARIF, DEVICE_STATUS,
        DEV_MODE_SRC, DEV_MODE, DEVICE, SENSOR, SENSOR_STATUS, DEVICE_STATE
    }

    private DataModel dm = new DataModel();
    private Set<Dictionary> loadedDictionaries = new HashSet<>();
    private Set<Dictionary> loadingDictionaries = new HashSet<>();
    private static final Set<Dictionary> DICTIONARIES_LIST = EnumSet.allOf(Dictionary.class);
    private final IBinder binder = new LocalBinder();
    private Thread mainThread;
    private Thread notificationsThread;
    private boolean notificationsLoading;

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
        mainThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mainLoop();
            }
        });
        mainThread.start();
        notificationsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                notificationsLoop();
            }
        });
        notificationsThread.start();
    }

    private void notificationsLoop() {
        while (!notificationsThread.interrupted()) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (!notificationsLoading && Utils.isNetworkAvailable(this)) {
                    long currentTime = new Date().getTime();
                    if (currentTime > time2loadNotifications) {
                        time2loadNotifications = currentTime + NOTIFICATIONS_REQUEST_PAUSE_MS;
                        loadNotifications();
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void getNotificationsSession(final Runnable onSuccess) {
        notificationsLoading = true;
        Request request = new Request().add(new GetNotifySessionCommand(getBaseContext()));
        Log.d(TAG, "get notifications session request: " + request);
        RequestTask requestTask = new RequestTask(Config.getNotifyServer(this)) {
            @Override
            public void onPostExecute(String result) {
                notificationsLoading = false;
                Log.d(TAG, "get notifications session response: " + result);
                NotifySession objects = new NotifySession(result);
                if (objects.isOk()) {
                    sessionId = objects.getSessionId();
                    onSuccess.run();
                }
            }

            @Override
            protected void onCancelled() {
                notificationsLoading = false;
            }

        };
        requestTask.execute(request.serialize());
    }

    private void loadNotifications() {
        if (sessionId == null) {
            getNotificationsSession(new Runnable() {
                @Override
                public void run() {
                    doNotificationsRequest();
                }
            });
        } else {
            doNotificationsRequest();
        }
    }

    private void doNotificationsRequest() {
        if (sessionId == null) {
            return;
        }
        notificationsLoading = true;
        Request request = new Request().add(new GetNotificationsCommand(sessionId));
        Log.d(TAG, "get notifications request: " + request);
        RequestTask requestTask = new RequestTask(Config.getNotifyServer(this)) {
            @Override
            public void onPostExecute(String result) {
                notificationsLoading = false;
                Log.d(TAG, "get notifications response: " + result);
                Notifications objects = new Notifications(result);
                if (objects.isOk()) {

//                    List<ru.steagle.request.responce.Notification> n = new ArrayList<>();
//                    ru.steagle.request.responce.Notification n1;
//                    n1 = new ru.steagle.request.responce.Notification();
//                    n1.setDate(new Date());
//                    n1.setText("tutu1");
//                    n.add(n1);
//                    n1 = new ru.steagle.request.responce.Notification();
//                    n1.setDate(new Date());
//                    n1.setText("tutu2");
//                    n.add(n1);
//                    showNotifications(n);

                    if (objects.getList().size() > 0)
                        showNotifications(objects.getList());
                } else {
                    sessionId = null;
                }
            }

            @Override
            protected void onCancelled() {
                notificationsLoading = false;
            }

        };
        requestTask.execute(request.serialize());
    }

    private void showNotifications(List<ru.steagle.datamodel.Notification> objects) {
        NotificationManager manager = (NotificationManager) getBaseContext().
                getSystemService(Context.NOTIFICATION_SERVICE);

        int def = (Config.isNotifySoundOn(getBaseContext()) ? Notification.DEFAULT_SOUND : 0) |
                (Config.isNotifyVibroOn(getBaseContext()) ? Notification.DEFAULT_VIBRATE : 0) | Notification.DEFAULT_LIGHTS;

        StringBuilder message = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("dd MMMM HH:mm:ss");
        for (ru.steagle.datamodel.Notification n : objects) {
            message.append(f.format(n.getDate())).append(": ").append(n.getText()).append("\n");
        }
        Intent notificationIntent = new Intent(getBaseContext(), NotificationsViewActivity.class); // по клику на уведомлении откроется HomeActivity

        notificationIntent.putExtra(NotificationsViewActivity.MESSAGE, message.toString());

        NotificationCompat.Builder nb = new NotificationCompat.Builder(getBaseContext())
                .setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(getString(R.string.new_notifications)) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message.toString()) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle(getString(R.string.new_notifications)) //заголовок уведомления
                .setDefaults(def) // звук, вибро и диодный индикатор выставляются по умолчанию
                .setNumber(objects.size());

        Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(notificationID++, notification); // отображаем его пользователю.
    }

    private void mainLoop() {
        while (!mainThread.interrupted()) {
            try {
                TimeUnit.SECONDS.sleep(1);
                if (Utils.isNetworkAvailable(this)) {
                    checkWaitingObjectsToLoad();
                    long currentTime = new Date().getTime();
                    if (loadedDictionaries.size() < DICTIONARIES_LIST.size() && currentTime > time2loadDictionaries) {
                        time2loadDictionaries = currentTime + DICTIONARY_REQUEST_PAUSE_MS;
                        loadDictionaries();
                    }
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void checkWaitingObjectsToLoad() {
        synchronized (waitingObjects) {
            if (waitingObjects.size() == 0)
                return;
            long currentTime = new Date().getTime();
            Iterator<WaitingObject> it = waitingObjects.iterator();
            while (it.hasNext()) {
                WaitingObject wo = it.next();
                if (currentTime > wo.time2load || currentTime > wo.time2stop) {
                    wo.time2load += 10000;
                    loadedDictionaries.remove(wo.object);
                }
                if (currentTime > wo.time2stop) {
                    it.remove();
                }

            }
        }
    }

    private void loadDictionaries() {
        Set<Dictionary> loadingDictionaries = new HashSet<>(DICTIONARIES_LIST);
        loadingDictionaries.removeAll(loadedDictionaries);
        for (Dictionary d : loadingDictionaries) {
            if (loadedDictionaries.contains(d))
                continue;
            if (Dictionary.USER_STATUS.equals(d)) {
                loadUserStatuses();
            } else if (Dictionary.CURRENCY.equals(d)) {
                loadCurrencies();
            } else if (Dictionary.TIME_ZONE.equals(d)) {
                loadTimeZones();
            } else if (Dictionary.TARIF.equals(d)) {
                loadTarifs();
            } else if (Dictionary.DEV_MODE.equals(d)) {
                loadDevModes();
            } else if (Dictionary.DEV_MODE_SRC.equals(d)) {
                loadDevModeSrcs();
            } else if (Dictionary.DEVICE.equals(d)) {
                loadDevices();
            } else if (Dictionary.SENSOR.equals(d)) {
                loadSensors();
            } else if (Dictionary.LEVEL.equals(d)) {
                loadLevels();
            } else if (Dictionary.DEVICE_STATUS.equals(d)) {
                loadDeviceStatuses();
            } else if (Dictionary.DEVICE_STATE.equals(d)) {
                loadDeviceStates();
            } else if (Dictionary.SENSOR_STATUS.equals(d)) {
                loadSensorStatuses();
            }
        }
    }

    private void loadDeviceStates() {
        final Dictionary dictionary = Dictionary.DEVICE_STATE;
        loadObjects(dictionary, new GetDeviceStatesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                DeviceStates objects = new DeviceStates(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setDeviceStates(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadSensorStatuses() {
        final Dictionary dictionary = Dictionary.SENSOR_STATUS;
        loadObjects(dictionary, new GetSensorStatusesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                SensorStatuses objects = new SensorStatuses(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setSensorStatuses(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadDeviceStatuses() {
        final Dictionary dictionary = Dictionary.DEVICE_STATUS;
        loadObjects(dictionary, new GetDeviceStatusesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                DeviceStatuses objects = new DeviceStatuses(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setDeviceStatuses(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadLevels() {
        final Dictionary dictionary = Dictionary.LEVEL;
        loadObjects(dictionary, new GetLevelsCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                Levels objects = new Levels(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setLevels(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadSensors() {
        if (dm.getDevices() == null || dm.getDevices().size() == 0)
            return;
        final Dictionary dictionary = Dictionary.SENSOR;
        List<Command> commandList = new ArrayList<>();
        for (Device d : dm.getDevices())
            commandList.add(new GetSensorsCommand(getBaseContext(), d.getId()));
        loadObjects(dictionary, commandList, new IResultProcessor() {
            @Override
            public void process(String result) {
                Sensors objects = new Sensors(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setSensors(objects.getList());
                    notifyObjectChanges(dictionary);
                    notifyObjectChanges(Dictionary.DEVICE);
                }
            }
        });

    }

    private void loadDevices() {
        final Dictionary dictionary = Dictionary.DEVICE;
        loadObjects(dictionary, new GetDevicesCommand(getBaseContext()), new IResultProcessor() {
            @Override
            public void process(String result) {
                Devices objects = new Devices(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setDevices(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadDevModeSrcs() {
        final Dictionary dictionary = Dictionary.DEV_MODE_SRC;
        loadObjects(dictionary, new GetDevModeSrcsCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                DevModeSrcs objects = new DevModeSrcs(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setDevModeSrcs(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadDevModes() {
        final Dictionary dictionary = Dictionary.DEV_MODE;
        loadObjects(dictionary, new GetDevModesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                DevModes objects = new DevModes(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setDevModes(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadTarifs() {
        final Dictionary dictionary = Dictionary.TARIF;
        loadObjects(dictionary, new GetTarifsCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                Tarifs objects = new Tarifs(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setTarifs(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadTimeZones() {
        final Dictionary dictionary = Dictionary.TIME_ZONE;
        loadObjects(dictionary, new GetTimeZonesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                TimeZones objects = new TimeZones(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setTimeZones(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadCurrencies() {
        final Dictionary dictionary = Dictionary.CURRENCY;
        loadObjects(dictionary, new GetCurrenciesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                Currencies objects = new Currencies(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setCurrencies(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
    }

    private void loadUserStatuses() {
        final Dictionary dictionary = Dictionary.USER_STATUS;
        loadObjects(dictionary, new GetUserStatusesCommand(), new IResultProcessor() {
            @Override
            public void process(String result) {
                UserStatuses objects = new UserStatuses(result);
                if (objects.isOk()) {
                    loadedDictionaries.add(dictionary);
                    dm.setUserStatuses(objects.getList());
                    notifyObjectChanges(dictionary);
                }
            }
        });
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
        if (loadingDictionaries.contains(dictionary))
            return;
        Log.d(TAG, "get " + dictionary + " list request: " + request);
        loadingDictionaries.add(dictionary);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {
            @Override
            public void onPostExecute(String result) {
                loadingDictionaries.remove(dictionary);
                Log.d(TAG, "get " + dictionary + " list response: " + result);

                resultProcessor.process(result);
            }

            @Override
            protected void onCancelled() {
                loadingDictionaries.remove(dictionary);
            }

        };
        requestTask.execute(request.serialize());
    }

    private void notifyObjectChanges(Dictionary dictionary) {
        Intent intent = new Intent(BROADCAST_ACTION);
        Log.d(TAG, "Service: notify about " + dictionary + " list changes");
        intent.putExtra(OBJECT_NAME, dictionary.toString());
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
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

        public void waitForDeviceMode(String deviceId, String modeId) {
            SteagleService.this.waitForDeviceMode(deviceId, modeId);
        }
    }

    private void waitForDeviceMode(String deviceId, String modeId) {
        dm.clearDeviceStatus(deviceId);
        notifyObjectChanges(Dictionary.DEVICE);
        // TODO Выполнить минутный запрос за статусом устройства
        waitingObjects.add(new WaitingObject(Dictionary.DEVICE, new Date().getTime() + 60000));
    }

    private void setDeviceName(String deviceId, String deviceName) {
        dm.setDeviceName(deviceId, deviceName);
        notifyObjectChanges(Dictionary.DEVICE);
    }

    private void clearUserData() {
        dm.setDevices(new ArrayList<Device>());
        dm.setSensors(new ArrayList<Sensor>());
        loadedDictionaries.remove(Dictionary.SENSOR);
        loadedDictionaries.remove(Dictionary.DEVICE);
        time2loadDictionaries = 0;
    }

}
