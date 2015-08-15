package ru.steagle.views;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.TimeZone;
import ru.steagle.datamodel.UserInfo;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangeTimeZoneCommand;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

import static ru.steagle.service.SteagleService.Dictionary.TIME_ZONE;

/**
 * Created by bmw on 08.02.14.
 */
public class TimeZoneActivity extends ListActivity {

    private final static String TAG = TimeZoneActivity.class.getName();
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private BroadcastReceiver broadcastReceiver;
    private List<TimeZone> timeZones;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_time_zones);

        ((TextView) findViewById(R.id.title)).setText(getString(R.string.timeZones));

        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(TIME_ZONE))) {
                    updateData();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroy();
    }

    private void updateData() {
        List<String> data = new ArrayList<>();
        int position = -1;
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = serviceConnector.getServiceBinder().getDataModel();
            timeZones = dm.getTimeZones();
            String timeZoneName = dm.getTimeZone() == null ? null : dm.getTimeZone().getName();
            if (timeZones != null) {
                int i = 0;
                for (TimeZone tz : timeZones) {
                    data.add(tz.getName());
                    if (timeZoneName != null && tz != null && timeZoneName.equals(tz.getName()))
                        position = i;
                    i++;
                }
            }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, data);
        setListAdapter(adapter);
        if (position >= 0) {
            getListView().setSelection(position);
            getListView().setItemChecked(position, true);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }

        final String timeZone = timeZones.get(position).getName();
        final ProgressDialog dialog = Utils.getProgressDialog(this);
        Request request = new Request().add(new ChangeTimeZoneCommand(this, timeZone));
        Log.d(TAG, "ChangeTimeZone request: " + request);
        RequestTask authTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                dialog.dismiss();
                Log.d(TAG, "ChangeTimeZone response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    serviceConnector.getServiceBinder().setTimeZoneId(timeZone);
                    Toast.makeText(TimeZoneActivity.this, getString(R.string.timeZoneSuccessUpdate), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(TimeZoneActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                dialog.show();
            }

        };
        authTask.execute(request.serialize());
    }

}
