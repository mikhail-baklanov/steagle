package ru.steagle.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import ru.steagle.R;
import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Sensor;
import ru.steagle.datamodel.SensorType;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangeSensorNameCommand;
import ru.steagle.protocol.request.ChangeSensorStatusCommand;
import ru.steagle.protocol.request.DeleteSensorCommand;
import ru.steagle.protocol.responce.SensorChange;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

/**
 * Created by bmw on 09.02.14.
 */
public class SensorsActivity extends Activity {
    private final static String TAG = SensorsActivity.class.getName();
    public static final String DEVICE_ID_PARAM = "deviceId";
    private DataModel dm;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private BroadcastReceiver broadcastReceiver;
    private LayoutInflater inflater;
    private String deviceId;

    private void refreshSensors() {

        findViewById(R.id.freeText).setVisibility(View.GONE);

        List<Sensor> list = dm == null ? new ArrayList<Sensor>() : dm.getSensorsOfDevice(deviceId);
        List<Sensor> allowList = new ArrayList<>();
        List<Sensor> suspendList = new ArrayList<>();
        List<Sensor> unstableList = new ArrayList<>();
        String allowStatusId = dm == null ? null : dm.getAllowSensorStatusId();
        String suspendStatusId = dm == null ? null : dm.getSuspendSensorStatusId();
        for (Sensor d : list) {
            if (allowStatusId != null && allowStatusId.equals(d.getStatusId()))
                allowList.add(d);
            else if (suspendStatusId != null && suspendStatusId.equals(d.getStatusId()))
                suspendList.add(d);
            else
                unstableList.add(d);
        }

        LinearLayout layout;
        TextView tv;
        layout = (LinearLayout) findViewById(R.id.turnOnSensorsContainer);
        fillSensorPart(allowList, R.id.turnOnSensorsPart, layout, R.drawable.switcher_on);

        layout = (LinearLayout) findViewById(R.id.turnOffSensorsContainer);
        fillSensorPart(suspendList, R.id.turnOffSensorsPart, layout, R.drawable.switcher_off);

        layout = (LinearLayout) findViewById(R.id.unstableSensorsContainer);
        tv = (TextView) findViewById(R.id.unstableSensorsHeader);
        tv.setText(getString(R.string.unstable_sensor_list));
        fillSensorPart(unstableList, R.id.unstableSensorsPart, layout, R.drawable.preloader);
    }

    private void showConnectingView() {
        findViewById(R.id.turnOnSensorsPart).setVisibility(View.GONE);
        findViewById(R.id.turnOffSensorsPart).setVisibility(View.GONE);
        findViewById(R.id.unstableSensorsPart).setVisibility(View.GONE);

        TextView tv = (TextView) findViewById(R.id.freeText);
        tv.setText(getString(R.string.getting_sensor_list));
        findViewById(R.id.freeText).setVisibility(View.VISIBLE);
    }

    private void fillSensorPart(List<Sensor> list, int sensorPartResourceId, LinearLayout layout, int statusResourceId) {
        layout.removeAllViews();
        if (list.size() == 0) {
            findViewById(sensorPartResourceId).setVisibility(View.GONE);
        } else {
            findViewById(sensorPartResourceId).setVisibility(View.VISIBLE);
            for (final Sensor item : list) {
                View v = inflater.inflate(R.layout.fragment_sensor_one, layout, false);
                String description = item.getDescription();
                if (description == null || description.trim().length() == 0)
                    description = getString(R.string.no_name_sensor);
                ((TextView) v.findViewById(R.id.sensorName)).setText(description);
                SensorType sensorType = dm==null ? null: dm.getSensorType(item.getTypeId());
                String sensorTypeName = "";
                if (sensorType != null)
                    sensorTypeName = sensorType.getDescription();
                ((TextView) v.findViewById(R.id.sensorType)).setText(sensorTypeName);
                v.findViewById(R.id.sensorName).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showActionsDialog(item);
                    }
                });
                ((ImageView) v.findViewById(R.id.sensorSwitch)).setImageResource(statusResourceId);
                v.findViewById(R.id.sensorSwitch).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeSensorStatus(item);
                    }
                });
                layout.addView(v);
            }
        }
    }

    private void changeSensorStatus(final Sensor item) {
        String allowStatusId = dm == null ? null : dm.getAllowSensorStatusId();
        String suspendStatusId = dm == null ? null : dm.getSuspendSensorStatusId();
        if (allowStatusId != null && allowStatusId.equals(item.getStatusId())) {
            turnSensorOff(item);
        } else if (suspendStatusId != null && suspendStatusId.equals(item.getStatusId())) {
            turnSensorOn(item);
        } else {
            Toast.makeText(this, getString(R.string.sensor_status_is_unstable), Toast.LENGTH_LONG).show();
        }
    }

    private void turnSensorOff(final Sensor item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        String suspendSensorStatusId = dm.getSuspendSensorStatusId();
        if (suspendSensorStatusId == null) {
            Toast.makeText(this, getString(R.string.sensor_statuses_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        changeSensorStatus(item.getId(), suspendSensorStatusId);
    }

    private void turnSensorOn(Sensor item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final String allowSensorStatusId = dm.getAllowSensorStatusId();
        if (allowSensorStatusId == null) {
            Toast.makeText(this, getString(R.string.sensor_statuses_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        changeSensorStatus(item.getId(), allowSensorStatusId);
    }

    private void changeSensorStatus(final String sensorId, final String statusId) {
        final ProgressDialog d = Utils.getProgressDialog(this);
        Request request = new Request().add(new ChangeSensorStatusCommand(this, sensorId, statusId));
        Log.d(TAG, "ChangeSensorStatus request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "ChangeSensorStatus response: " + result);

                SensorChange sensorChange = new SensorChange(result);
                if (sensorChange.isOk()) {
                    serviceConnector.getServiceBinder().waitForSensorStatus(deviceId, sensorId, statusId);
                } else {
                    Toast.makeText(SensorsActivity.this, sensorChange.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                d.show();
            }

        };
        requestTask.execute(request.serialize());

    }

    @Override
    public void onDestroy() {
        serviceConnector.unbind();
        this.unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void showActionsDialog(final Sensor item) {
        final CharSequence[] items = {
                getResources().getString(R.string.action_edit),
                getResources().getString(R.string.action_delete)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.getDescription());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemIndex) {
                if (itemIndex == 0) {
                    // Редактировать
                    TextEditDialogFragment d = new TextEditDialogFragment(
                            item.getDescription(), getString(R.string.labelSensor), item.getDescription(), getString(R.string.btnSave), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                        @Override
                        public void onYesClick(String oldValue, String value, Dialog dialog) {
                            saveSensorName(item.getId(), value, dialog);
                        }
                    });
                    d.show(getFragmentManager(), null);
                }
                if (itemIndex == 1) {
                    // удалить
                    Utils.showConfirmDialog(SensorsActivity.this, getString(R.string.sensor_deletion), getString(R.string.confirm_sensor_deletion),
                            getString(R.string.btnYes), getString(R.string.btnCancel), new Runnable() {
                        @Override
                        public void run() {
                            deleteSensor(item);
                        }
                    });
                }
            }
        }).show();

    }

    private void deleteSensor(final Sensor item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }

        final ProgressDialog d = Utils.getProgressDialog(this);
        Request request = new Request().add(new DeleteSensorCommand(this, item.getId()));
        Log.d(TAG, "DeleteSensor request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "DeleteSensor response: " + result);

                SensorChange sensorChange = new SensorChange(result);
                if (sensorChange.isOk()) {
                    Toast.makeText(SensorsActivity.this, getString(R.string.delete_sensor_request_sent), Toast.LENGTH_LONG).show();
                    serviceConnector.getServiceBinder().deleteSensor(item.getId());
                } else {
                    Toast.makeText(SensorsActivity.this, sensorChange.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                d.show();
            }

        };
        requestTask.execute(request.serialize());
    }

    private void saveSensorName(final String sensorId, final String sensorName, final Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }

        final ProgressDialog d = Utils.getProgressDialog(this);
        Request request = new Request().add(new ChangeSensorNameCommand(this, sensorId, sensorName));
        Log.d(TAG, "ChangeSensorName request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "ChangeSensorName response: " + result);

                SensorChange sensorChange = new SensorChange(result);
                if (sensorChange.isOk()) {
                    serviceConnector.getServiceBinder().setSensorName(sensorId, sensorName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(SensorsActivity.this, sensorChange.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                d.show();
            }

        };
        requestTask.execute(request.serialize());
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sensors);
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        deviceId = getIntent().getStringExtra(DEVICE_ID_PARAM);

        ((TextView) findViewById(R.id.title)).setText(getString(R.string.sensors));
        ((TextView) findViewById(R.id.operation_text)).setText(getString(R.string.btnAdd));
        findViewById(R.id.operation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addSensor();
            }
        });

        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                dm = serviceConnector.getServiceBinder().getDataModel();
                refreshSensors();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(SteagleService.Dictionary.SENSOR,
                        SteagleService.Dictionary.SENSOR_TYPE, SteagleService.Dictionary.SENSOR_STATUS_CHANGE))) {
                    refreshSensors();
                }
            }
        };
        showConnectingView();
        this.registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
    }

    private void addSensor() {
        Intent intent = new Intent(this, SensorAddActivity.class);
        intent.putExtra(SensorAddActivity.DEVICE_ID_PARAM, deviceId);
        startActivity(intent);
    }

}