package ru.steagle.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.R;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangeNotifyFlagCommand;
import ru.steagle.datamodel.UserInfo;

/**
 * Created by bmw on 09.02.14.
 */
public class NotificationsActivity extends Activity {
    private final static String TAG = NotificationsActivity.class.getName();
    private ImageView phoneSwitch;
    private ImageView smsSwitch;
    private ImageView emailSwitch;
    private DataModel dm;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);

    private void refreshNotifyData() {
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layout = (LinearLayout) findViewById(R.id.phoneNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationPhoneList() != null) {
            for (final String item : dm.getNotificationPhoneList()) {
                View v = inflater.inflate(R.layout.notification_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                layout.addView(v);
            }
        }
        layout = (LinearLayout) findViewById(R.id.smsNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationSmsList() != null) {
            for (final String item : dm.getNotificationSmsList()) {
                View v = inflater.inflate(R.layout.notification_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                layout.addView(v);
            }
        }
        layout = (LinearLayout) findViewById(R.id.emailNotificationsContainer);
        layout.removeAllViews();
        if (dm.getNotificationEmailList() != null) {
            for (final String item : dm.getNotificationEmailList()) {
                View v = inflater.inflate(R.layout.notification_one, layout, false);
                ((TextView) v.findViewById(R.id.message)).setText(item);
                layout.addView(v);
            }
        }

    }

    @Override
    public void onDestroy() {
        serviceConnector.unbind();
        super.onDestroy();
    }

	private void startNotificationsEditActivity() {
		Intent intent = new Intent(this, NotificationsEditActivity.class);
		startActivity(intent);
		finish();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications);

        ((TextView)findViewById(R.id.title)).setText(getString(R.string.notifications));
        ((TextView)findViewById(R.id.operation_text)).setText(getString(R.string.btnEdit));
        findViewById(R.id.operation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				startNotificationsEditActivity();
            }
        });

        phoneSwitch = (ImageView)findViewById(R.id.phoneSwitch);
        smsSwitch = (ImageView)findViewById(R.id.smsSwitch);
        emailSwitch = (ImageView)findViewById(R.id.emailSwitch);

        phoneSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToSwitch(ChangeNotifyFlagCommand.PHONE, !dm.isPhoneNotifyEnabled());
            }
        });
        smsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToSwitch(ChangeNotifyFlagCommand.SMS, !dm.isSmsNotifyEnabled());
            }
        });
        emailSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryToSwitch(ChangeNotifyFlagCommand.EMAIL, !dm.isEmailNotifyEnabled());
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

    private void tryToSwitch(final String flag, final boolean enable) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final ProgressDialog dialog = Utils.getProgressDialog(this);
        Request request = new Request().add(new ChangeNotifyFlagCommand(this, flag, enable));
        Log.d(TAG, "ChangeNotifyFlag request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                dialog.dismiss();
                Log.d(TAG, "ChangeNotifyFlag response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    switch (flag) {
                        case ChangeNotifyFlagCommand.PHONE:
                            dm.setPhoneNotifyEnabled(enable);
                            break;
                        case ChangeNotifyFlagCommand.SMS:
                            dm.setSmsNotifyEnabled(enable);
                            break;
                        case ChangeNotifyFlagCommand.EMAIL:
                            dm.setEmailNotifyEnabled(enable);
                            break;
                    }
                    refreshNotifySwitches();
                } else {
                    Toast.makeText(NotificationsActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                dialog.show();
            }

        };
        requestTask.execute(request.serialize());

    }

    private void refreshNotifications() {
        refreshNotifySwitches();
        refreshNotifyData();
    }

    private void refreshNotifySwitches() {
        phoneSwitch.setImageResource(dm.isPhoneNotifyEnabled() ? R.drawable.switcher_on : R.drawable.switcher_off);
        smsSwitch.setImageResource(dm.isSmsNotifyEnabled() ? R.drawable.switcher_on : R.drawable.switcher_off);
        emailSwitch.setImageResource(dm.isEmailNotifyEnabled() ? R.drawable.switcher_on : R.drawable.switcher_off);
    }
}