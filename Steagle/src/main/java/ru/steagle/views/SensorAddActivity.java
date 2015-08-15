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
import java.util.Iterator;
import java.util.List;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.SensorType;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.AddSensorCommand;
import ru.steagle.protocol.responce.SensorChange;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

import static ru.steagle.service.SteagleService.Dictionary.SENSOR_TYPE;

/**
 * Created by bmw on 08.02.14.
 */
public class SensorAddActivity extends ListActivity {
    public final static String DEVICE_ID_PARAM = "deviceId";
    private final static String TAG = SensorAddActivity.class.getName();
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private BroadcastReceiver broadcastReceiver;
    private List<SensorType> sensorTypes;
    private DataModel dm;
    private String sensorTypeId;
    private String deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sensor_add);

        View header = getLayoutInflater().inflate(R.layout.fragment_sensor_add_header, null);
        View footer = getLayoutInflater().inflate(R.layout.fragment_sensor_add_footer, null);
        deviceId = getIntent().getStringExtra(DEVICE_ID_PARAM);

        ((TextView) header.findViewById(R.id.title)).setText(getString(R.string.sensor_adding));

        footer.findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSensor();
            }
        });
        getListView().addHeaderView(header, null, false);
        getListView().addFooterView(footer, null, false);
        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                dm = serviceConnector.getServiceBinder().getDataModel();
                updateData();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(SENSOR_TYPE))) {
                    updateData();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        updateData();
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
        sensorTypes = new ArrayList<>();
        if (dm != null) {
            sensorTypes.addAll(dm.getSensorTypesList());
            if (sensorTypes != null) {
                // удаляем странные
                Iterator<SensorType> iterator = sensorTypes.iterator();
                while (iterator.hasNext()) {
                    SensorType st = iterator.next();
                    if (st.getDescription() == null || st.getDescription().trim().length() == 0 ||
                            getString(R.string.sensor_bad_name1).equals(st.getDescription()))
                        iterator.remove();
                }

                for (SensorType st : sensorTypes) {
                    data.add(st.getDescription());
                }
            }
        }
//        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, data);
        setListAdapter(adapter);
        if (sensorTypes.size() >= 0)
            setSelection(position);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        setSensorTypeId(sensorTypes.get(position - 1).getId()); // -1 из-за строки-заголовка
    }

    public void addSensor() {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        if (sensorTypeId == null || sensorTypeId.trim().length() == 0) {
            Toast.makeText(this, getString(R.string.should_choose_sensor_type), Toast.LENGTH_LONG).show();
            return;
        }

        final ProgressDialog dialog = Utils.getProgressDialog(this);
        Request request = new Request().add(new AddSensorCommand(this, deviceId, sensorTypeId));
        Log.d(TAG, "AddSensor request: " + request);
        RequestTask authTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                dialog.dismiss();
                Log.d(TAG, "AddSensor response: " + result);
                SensorChange userInfo = new SensorChange(result);
                if (userInfo.isOk()) {
                    serviceConnector.getServiceBinder().waitForSensorAdded();
                    Toast.makeText(SensorAddActivity.this, getString(R.string.sensor_added), Toast.LENGTH_LONG).show();
                    SensorAddActivity.this.finish();
                } else {
                    Toast.makeText(SensorAddActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                dialog.show();
            }

        };
        authTask.execute(request.serialize());
    }

    public void setSensorTypeId(String sensorTypeId) {
        this.sensorTypeId = sensorTypeId;
    }
}
