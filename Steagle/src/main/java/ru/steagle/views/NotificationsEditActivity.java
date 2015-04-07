package ru.steagle.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.UserInfo;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangeNotifyCommand;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

/**
 * Created by bmw on 09.02.14.
 */
public class NotificationsEditActivity extends Activity {
    private final static String TAG = NotificationsEditActivity.class.getName();
    private DataModel dm;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);

    private void refreshNotifications() {
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) findViewById(R.id.phoneNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationPhoneList() != null) {
            for (final String item : dm.getNotificationPhoneList()) {
                View v = inflater.inflate(R.layout.notification_edit_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                v.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextEditDialogFragment dialog = new TextEditDialogFragment(
                                getString(R.id.updatePhone), getString(R.id.labelPhone), item, getString(R.id.btnUpdate), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                            @Override
                            public void onYesClick(String oldValue, String value, Dialog dialog) {
                                updatePhone(oldValue, value, dialog);
                            }
                        });
                        dialog.show(getFragmentManager(), null);
                    }
                });
                v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showConfirmDialog(NotificationsEditActivity.this, null, getString(R.string.confirmPhoneDelete), getString(R.string.btnYes), getString(R.id.btnCancel), new Runnable() {
                            @Override
                            public void run() {
                                deletePhone(item);
                            }
                        });
                    }
                });
                layout.addView(v);
            }
        }
        layout = (LinearLayout) findViewById(R.id.smsNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationSmsList() != null) {
            for (final String item : dm.getNotificationSmsList()) {
                View v = inflater.inflate(R.layout.notification_edit_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                v.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextEditDialogFragment dialog = new TextEditDialogFragment(
                                getString(R.id.updateSms), getString(R.id.labelSms), item, getString(R.id.btnUpdate), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                            @Override
                            public void onYesClick(String oldValue, String value, Dialog dialog) {
                                updateSms(oldValue, value, dialog);
                            }
                        });
                        dialog.show(getFragmentManager(), null);

                    }
                });
                v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showConfirmDialog(NotificationsEditActivity.this, null, getString(R.string.confirmSmsDelete), getString(R.string.btnYes), getString(R.id.btnCancel), new Runnable() {
                            @Override
                            public void run() {
                                deleteSms(item);
                            }
                        });

                    }
                });
                layout.addView(v);
            }
        }
        layout = (LinearLayout) findViewById(R.id.emailNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationEmailList() != null) {
            for (final String item : dm.getNotificationEmailList()) {
                View v = inflater.inflate(R.layout.notification_edit_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                v.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TextEditDialogFragment dialog = new TextEditDialogFragment(
                                getString(R.id.updateEmail), getString(R.id.labelEmail), item, getString(R.id.btnUpdate), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                            @Override
                            public void onYesClick(String oldValue, String value, Dialog dialog) {
                                updateEmail(oldValue, value, dialog);
                            }
                        });
                        dialog.show(getFragmentManager(), null);

                    }
                });
                v.findViewById(R.id.btnDelete).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.showConfirmDialog(NotificationsEditActivity.this, null,
                                getString(R.string.confirmEmailDelete), getString(R.string.btnYes), getString(R.id.btnCancel), new Runnable() {
                            @Override
                            public void run() {
                                deleteEmail(item);

                            }
                        });

                    }
                });
                layout.addView(v);
            }
        }
    }

    private void deleteEmail(String item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        deleteValue(item, dm.getNotificationEmailList(), ChangeNotifyCommand.NOTIFY_OBJECT.EMAIL);

    }

    private void deleteSms(String item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        deleteValue(item, dm.getNotificationSmsList(), ChangeNotifyCommand.NOTIFY_OBJECT.SMS);
    }

    private void deletePhone(String item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        deleteValue(item, dm.getNotificationPhoneList(), ChangeNotifyCommand.NOTIFY_OBJECT.PHONE);
    }

    private void updateEmail(String oldValue, String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        updateValue(oldValue, value, dm.getNotificationEmailList(), ChangeNotifyCommand.NOTIFY_OBJECT.EMAIL, dialog);
    }

    private void updateSms(String oldValue, String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        updateValue(oldValue, value, dm.getNotificationSmsList(), ChangeNotifyCommand.NOTIFY_OBJECT.SMS, dialog);
    }

    private void updatePhone(String oldValue, String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        updateValue(oldValue, value, dm.getNotificationPhoneList(), ChangeNotifyCommand.NOTIFY_OBJECT.PHONE, dialog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications_edit);

        ((TextView)findViewById(R.id.title)).setText(getString(R.string.notifications));

        findViewById(R.id.btnAddPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextEditDialogFragment dialog = new TextEditDialogFragment(
                        getString(R.id.addPhone), getString(R.id.labelPhone), "", getString(R.id.btnSave), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String oldValue, String value, Dialog dialog) {
                        addPhone(value, dialog);
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        findViewById(R.id.btnAddSms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextEditDialogFragment dialog = new TextEditDialogFragment(
                        getString(R.id.addSms), getString(R.id.labelSms), "", getString(R.id.btnSave), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String oldValue, String value, Dialog dialog) {
                        addSms(value, dialog);
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        findViewById(R.id.btnAddEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextEditDialogFragment dialog = new TextEditDialogFragment(
                        getString(R.id.addEmail), getString(R.id.labelEmail), "", getString(R.id.btnSave), getString(R.id.btnCancel), new TextEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String oldValue, String value, Dialog dialog) {
                        addEmail(value, dialog);
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });
        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                dm = serviceConnector.getServiceBinder().getDataModel();
                refreshNotifications();
            }
        });
    }

    public void updateItem(List<String> list, String oldValue, String value) {
        for (int i = 0; i < list.size(); i++)
            if (oldValue.equals(list.get(i))) {
                list.set(i, value);
            }
    }

    public void deleteItem(List<String> list, String value) {
        list.remove(value);
    }

    @Override
    public void onDestroy() {
        serviceConnector.unbind();
        super.onDestroy();
    }

    private void addEmail(String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        addValue(value, dm.getNotificationEmailList(), ChangeNotifyCommand.NOTIFY_OBJECT.EMAIL, dialog);
    }

    private void addSms(String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        addValue(value, dm.getNotificationSmsList(), ChangeNotifyCommand.NOTIFY_OBJECT.SMS, dialog);
    }

    private void addPhone(final String value, Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        addValue(value, dm.getNotificationPhoneList(), ChangeNotifyCommand.NOTIFY_OBJECT.PHONE, dialog);
    }

    private void addValue(final String value, final List<String> originalList, ChangeNotifyCommand.NOTIFY_OBJECT notifyObject, final Dialog dialog) {

        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final ProgressDialog progressDialog = Utils.getProgressDialog(this);
        List<String> list = new ArrayList<>(originalList);
        list.add(value);
        Request request = new Request().add(new ChangeNotifyCommand(this, notifyObject, list));
        Log.d(TAG, "ChangeNotifyFlag request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                progressDialog.dismiss();
                Log.d(TAG, "ChangeNotify response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    dialog.cancel();
                    originalList.add(value);
                    refreshNotifications();
                } else {
                    Toast.makeText(NotificationsEditActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                progressDialog.show();
            }

        };
        requestTask.execute(request.serialize());

    }

    private void updateValue(final String oldValue, final String value, final List<String> originalList, ChangeNotifyCommand.NOTIFY_OBJECT notifyObject, final Dialog dialog) {

        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final ProgressDialog progressDialog = Utils.getProgressDialog(this);
        List<String> list = new ArrayList<>(originalList);
        updateItem(list, oldValue, value);
        Request request = new Request().add(new ChangeNotifyCommand(this, notifyObject, list));
        Log.d(TAG, "ChangeNotifyFlag request: " + request);
        RequestTask authTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                progressDialog.dismiss();
                Log.d(TAG, "ChangeNotify response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    dialog.cancel();
                    updateItem(originalList, oldValue, value);
                    refreshNotifications();
                } else {
                    Toast.makeText(NotificationsEditActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                progressDialog.show();
            }

        };
        authTask.execute(request.serialize());

    }

    private void deleteValue(final String value, final List<String> originalList, ChangeNotifyCommand.NOTIFY_OBJECT notifyObject) {

        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final ProgressDialog progressDialog = Utils.getProgressDialog(this);
        List<String> list = new ArrayList<>(originalList);
        deleteItem(list, value);
        Request request = new Request().add(new ChangeNotifyCommand(this, notifyObject, list));
        Log.d(TAG, "ChangeNotifyFlag request: " + request);
        RequestTask authTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                progressDialog.dismiss();
                Log.d(TAG, "ChangeNotify response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    deleteItem(originalList, value);
                    refreshNotifications();
                } else {
                    Toast.makeText(NotificationsEditActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                progressDialog.show();
            }

        };
        authTask.execute(request.serialize());

    }
}