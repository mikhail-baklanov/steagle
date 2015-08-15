package ru.steagle.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.List;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.config.Keys;
import ru.steagle.datamodel.Notification;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.request.GetNotificationsCommand;
import ru.steagle.protocol.request.GetNotifySessionCommand;
import ru.steagle.protocol.responce.Notifications;
import ru.steagle.protocol.responce.NotifySession;
import ru.steagle.views.NotificationsViewActivity;

public class NotificationRequestCommand extends BaseRequestCommand implements IRequestDataCommand {

    public static final long LOADING_PAUSE_MS = 10000;
    private static final String TAG = NotificationRequestCommand.class.getName();
    private static final long LOADING_ERROR_PAUSE_MS = 30000;
    protected String sessionId;
    protected Context context;
    private int notificationID;

    public NotificationRequestCommand(Context context) {
        this.context = context;
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
    public void run() {
        if (sessionId == null) {
            canRun = false;
            getNotificationsSession(new AsyncRun() {
                @Override
                public void onSuccess(Object data) {
                    if (sessionId == null)
                        time2load = System.currentTimeMillis() + LOADING_ERROR_PAUSE_MS;
                    else
                        time2load = System.currentTimeMillis() + 0; // получили сессию, сразу выполняем запрос оповещений
                    canRun = true;
                }

                @Override
                public void onFailure() {
                    time2load = System.currentTimeMillis() + LOADING_ERROR_PAUSE_MS; // при ошибке немного подождем
                    canRun = true;
                }
            });
        } else {
            canRun = false;
            doNotificationsRequest(new AsyncRun<List<Notification>>() {
                @Override
                public void onSuccess(List<Notification> data) {
                    if (data.size() > 0)
                        time2load = System.currentTimeMillis() + 0; // получили данные, сразу выполняем следующий запрос
                    else
                        time2load = System.currentTimeMillis() + LOADING_PAUSE_MS; // данные не пришли, ждем
                    canRun = true;
                }

                @Override
                public void onFailure() {
                    sessionId = null;
                    time2load = System.currentTimeMillis() + 0; // ошибка, сразу выполняем следующий запрос
                    canRun = true;
                }
            });
        }
    }

    private void doNotificationsRequest(final AsyncRun<List<Notification>> asyncRun) {
        if (sessionId == null) {
            asyncRun.onFailure();
        }
        Request request = new Request().add(new GetNotificationsCommand(sessionId));
        Log.d(TAG, "get notifications request: " + request);
        ServiceTask requestTask = new ServiceTask(Config.getNotifyServer(context));
        String result = requestTask.execute(request.serialize());
        Log.d(TAG, "get notifications response: " + result);
        Notifications objects = new Notifications(result);
        if (objects.isOk()) {
            if (objects.getList().size() > 0)
                showNotifications(objects.getList());
            asyncRun.onSuccess(objects.getList());
        } else {
            asyncRun.onFailure();
        }
    }

    private void showNotifications(List<Notification> objects) {
        NotificationManager manager = (NotificationManager) context.
                getSystemService(Context.NOTIFICATION_SERVICE);

        int def = (Config.isNotifySoundOn(context) ? android.app.Notification.DEFAULT_SOUND : 0) |
                (Config.isNotifyVibroOn(context) ? android.app.Notification.DEFAULT_VIBRATE : 0) |
                android.app.Notification.DEFAULT_LIGHTS;

        StringBuilder message = new StringBuilder();
        SimpleDateFormat f = new SimpleDateFormat("dd MMMM HH:mm:ss");
        for (ru.steagle.datamodel.Notification n : objects) {
            message.append(f.format(n.getDate())).append(": ").append(n.getText()).append("\n");
        }
        Intent notificationIntent = new Intent(context, NotificationsViewActivity.class); // по клику на уведомлении откроется HomeActivity

        notificationIntent.putExtra(NotificationsViewActivity.MESSAGE, message.toString());

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_launcher_grey) //иконка уведомления
                .setAutoCancel(true) //уведомление закроется по клику на него
                .setTicker(context.getString(R.string.new_notifications)) //текст, который отобразится вверху статус-бара при создании уведомления
                .setContentText(message.toString()) // Основной текст уведомления
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setWhen(System.currentTimeMillis()) //отображаемое время уведомления
                .setContentTitle(context.getString(R.string.new_notifications)) //заголовок уведомления
                .setDefaults(def) // звук, вибро и диодный индикатор
                .setNumber(objects.size());

        android.app.Notification notification = nb.getNotification(); //генерируем уведомление
        manager.notify(notificationID++, notification); // отображаем его пользователю.
    }

//
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
//
//                    if (objects.getList().size() > 0)
//                        showNotifications(objects.getList());
//                } else {
//                    sessionId = null;
//                }
//            }


    private void getNotificationsSession(final AsyncRun asyncRun) {
        Request request = new Request().add(new GetNotifySessionCommand(context));
        Log.d(TAG, "get notifications session request: " + request);
        ServiceTask requestTask = new ServiceTask(Config.getNotifyServer(context));
        String result = requestTask.execute(request.serialize());
        Log.d(TAG, "get notifications session response: " + result);
        NotifySession objects = new NotifySession(result);
        if (objects.isOk()) {
            sessionId = objects.getSessionId();
            asyncRun.onSuccess(null);
        } else {
            asyncRun.onFailure();
        }
    }

}
