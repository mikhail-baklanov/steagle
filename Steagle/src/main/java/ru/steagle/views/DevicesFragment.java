package ru.steagle.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
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
import android.view.ViewGroup;
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
import ru.steagle.datamodel.Device;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangeDeviceModeCommand;
import ru.steagle.protocol.request.ChangeDeviceNameCommand;
import ru.steagle.protocol.request.ChangeMasterPasswordCommand;
import ru.steagle.protocol.responce.DeviceChange;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;

/**
 * Created by bmw on 09.02.14.
 */
public class DevicesFragment extends Fragment {
    private final static String TAG = DevicesFragment.class.getName();
    private View view;
    private DataModel dm;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private BroadcastReceiver broadcastReceiver;
    private static String[] devicesNumbersText = new String[]{"устройство", "устройства", "устройств"};
    private LayoutInflater inflater;

    private boolean isOnLine(Device d) {
        String onlineState = dm == null ? null : dm.getOnlineDeviceStateId();
        return onlineState != null && onlineState.equals(d.getStateId());
    }

    private boolean isOn(Device d) {
        String allowStatusId = dm == null ? null : dm.getAllowDeviceStatusId();
        String onlineState = dm == null ? null : dm.getOnlineDeviceStateId();
        return allowStatusId != null && allowStatusId.equals(d.getStatusId()) &&
                onlineState != null && onlineState.equals(d.getStateId()) &&
                dm != null && dm.getOnlineDeviceLModeId(d.getLastModeId());
    }

    private void refreshDevices() {

        view.findViewById(R.id.freeText).setVisibility(View.GONE);

        List<Device> list = dm == null ? new ArrayList<Device>() : dm.getDevices();
        List<Device> allowList = new ArrayList<>();
        List<Device> suspendList = new ArrayList<>();
        List<Device> offlineList = new ArrayList<>();
        for (Device d : list) {
            if (isOnLine(d)) {
                if (isOn(d))
                    allowList.add(d);
                else
                    suspendList.add(d);
            } else
                offlineList.add(d);
        }

        LinearLayout layout;
        TextView tv;
        layout = (LinearLayout) view.findViewById(R.id.turnOnDevicesContainer);
        tv = (TextView) view.findViewById(R.id.turnOnDevicesHeader);
        if (allowList.size() == 1)
            tv.setText("Устройство на охране".toUpperCase());
        else
            tv.setText(("" + allowList.size() + " " +
                    devicesNumbersText[Utils.getDeclinationIndex(allowList.size())] + " в режиме охраны").toUpperCase());
        fillDevicePart(allowList, R.id.turnOnDevicesPart, layout, R.drawable.lock_green);

        layout = (LinearLayout) view.findViewById(R.id.turnOffDevicesContainer);
        tv = (TextView) view.findViewById(R.id.turnOffDevicesHeader);
        if (suspendList.size() == 1)
            tv.setText("Устройство снято с охраны".toUpperCase());
        else
            tv.setText(("" + suspendList.size() + " " +
                    devicesNumbersText[Utils.getDeclinationIndex(suspendList.size())] + " снято с охраны").toUpperCase());
        fillDevicePart(suspendList, R.id.turnOffDevicesPart, layout, R.drawable.lock_gray);

        layout = (LinearLayout) view.findViewById(R.id.unstableDevicesContainer);
        tv = (TextView) view.findViewById(R.id.unstableDevicesHeader);
        tv.setText(getString(R.string.unstable_device_list));
        if (offlineList.size() == 1)
            tv.setText("Устройство не в сети".toUpperCase());
        else
            tv.setText(("" + offlineList.size() + " " +
                    devicesNumbersText[Utils.getDeclinationIndex(offlineList.size())] + " не в сети").toUpperCase());
        fillDevicePart(offlineList, R.id.unstableDevicesPart, layout, R.drawable.lock_gray);
    }

    private void showConnectingView() {

        view.findViewById(R.id.turnOnDevicesPart).setVisibility(View.GONE);
        view.findViewById(R.id.turnOffDevicesPart).setVisibility(View.GONE);
        view.findViewById(R.id.unstableDevicesPart).setVisibility(View.GONE);

        TextView tv = (TextView) view.findViewById(R.id.freeText);
        tv.setText(getString(R.string.getting_device_list));
        view.findViewById(R.id.freeText).setVisibility(View.VISIBLE);

    }

    private void fillDevicePart(List<Device> list, int devicePartResourceId, LinearLayout layout, int statusResourceId) {
        layout.removeAllViews();
        if (list.size() == 0) {
            view.findViewById(devicePartResourceId).setVisibility(View.GONE);
        } else {
            view.findViewById(devicePartResourceId).setVisibility(View.VISIBLE);
            for (final Device item : list) {
                View v = inflater.inflate(R.layout.fragment_device_one, layout, false);
                ((TextView) v.findViewById(R.id.deviceName)).setText(item.getDescription());
                ((ImageView) v.findViewById(R.id.deviceLock)).setImageResource(statusResourceId);
                v.findViewById(R.id.deviceLock).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeDeviceStatus(item);
                    }
                });
                v.findViewById(R.id.sensorsInfo).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        runSensorsActivity(item);
                    }
                });
                v.findViewById(R.id.deviceName).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showActionsDialog(item);
                    }
                });
                int onCounter = item.getSensorOnCounter();
                int offCounter = item.getSensorOffCounter();
                ((ImageView) v.findViewById(R.id.sensorsStatus)).setImageResource(
                        onCounter > 0 ? R.drawable.eye_green : R.drawable.eye_gray);
                ((TextView) v.findViewById(R.id.counter)).setText(
                        "" + onCounter + "/" + (onCounter + offCounter));
                layout.addView(v);
            }
        }
    }

    private void runSensorsActivity(Device item) {
        Intent intent = new Intent(getActivity(), SensorsActivity.class);
        intent.putExtra(SensorsActivity.DEVICE_ID_PARAM, item.getId());
        startActivity(intent);
    }

    private void showActionsDialog(final Device item) {
        final CharSequence[] items = {
                getActivity().getResources().getString(R.string.action_edit),
                getActivity().getResources().getString(R.string.change_master_password)};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(item.getDescription());
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int itemIndex) {
                if (itemIndex == 0) {
                    // Редактировать
                    TextEditDialogFragment d = new TextEditDialogFragment(
                            item.getDescription(), getString(R.string.labelDevice), item.getDescription(), getString(R.string.btnSave), getString(R.string.btnCancel), new TextEditDialogFragment.Listener() {
                        @Override
                        public void onYesClick(String oldValue, String value, Dialog dialog) {
                            saveDeviceName(item.getId(), value, dialog);
                        }
                    });
                    d.show(getFragmentManager(), null);
                } else if (itemIndex == 1) {
                    confirmChangePasswordDialog(new Runnable() {
                        @Override
                        public void run() {
                            changeMasterPassword(item);
                        }
                    });
                }
            }
        }).show();

    }

    private void confirmChangePasswordDialog(final Runnable action) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(R.string.beforeChangePasswordMessage)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        action.run();
                    }
                })
                .show();
    }

    private void changeMasterPassword(final Device item) {
        // Изменить мастер пароль устройства
        MasterPasswordDialogFragment d = new MasterPasswordDialogFragment(
                new MasterPasswordDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String password, Dialog dialog) {
                        saveMasterPassword(item.getId(), password, dialog);
                    }
                });
        d.show(getFragmentManager(), null);
    }

    private void saveMasterPassword(String deviceId, String password, final Dialog dialog) {
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showNetworkError(getActivity());
            return;
        }

        final ProgressDialog d = Utils.getProgressDialog(getActivity());
        Request request = new Request().add(new ChangeMasterPasswordCommand(getActivity(), deviceId, password));
        Log.d(TAG, "ChangeMasterPassword request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(getActivity())) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "ChangeMasterPassword response: " + result);

                DeviceChange deviceChange = new DeviceChange(result);
                if (deviceChange.isOk()) {
                    dialog.dismiss();
                    Toast.makeText(getActivity(), getString(R.string.master_password_change_success), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), deviceChange.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                d.show();
            }

        };
        requestTask.execute(request.serialize());
    }

    private void saveDeviceName(final String deviceId, final String deviceName, final Dialog dialog) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(getActivity());
            return;
        }
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showNetworkError(getActivity());
            return;
        }

        final ProgressDialog d = Utils.getProgressDialog(getActivity());
        Request request = new Request().add(new ChangeDeviceNameCommand(getActivity(), deviceId, deviceName));
        Log.d(TAG, "ChangeDeviceName request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(getActivity())) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "ChangeDeviceName response: " + result);

                DeviceChange deviceChange = new DeviceChange(result);
                if (deviceChange.isOk()) {
                    serviceConnector.getServiceBinder().setDeviceName(deviceId, deviceName);
                    dialog.dismiss();
                } else {
                    Toast.makeText(getActivity(), deviceChange.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                d.show();
            }

        };
        requestTask.execute(request.serialize());
    }

    private void changeDeviceStatus(final Device item) {
        if (isOn(item))
            Utils.showConfirmDialog(getActivity(), getString(R.string.turn_off_guard), getString(R.string.confirm_turn_off_guard),
                    getString(R.string.btnYes), getString(R.string.btnCancel), new Runnable() {
                @Override
                public void run() {
                    turnDeviceOff(item);
                }
            });
        else
            Utils.showConfirmDialog(getActivity(), getString(R.string.turn_on_guard), getString(R.string.confirm_turn_on_guard),
                    getString(R.string.btnYes), getString(R.string.btnCancel), new Runnable() {
                @Override
                public void run() {
                    turnDeviceOn(item);
                }
            });
    }

    private void turnDeviceOff(final Device item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(getActivity());
            return;
        }
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showNetworkError(getActivity());
            return;
        }
        String suspendDeviceModeId = dm.getSuspendDeviceModeId();
        String suspendStatusId = dm == null ? null : dm.getSuspendDeviceStatusId();
        if (suspendDeviceModeId == null) {
            Toast.makeText(getActivity(), getString(R.string.device_modes_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        if (suspendStatusId == null) {
            Toast.makeText(getActivity(), getString(R.string.device_statuses_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        changeDeviceMode(item.getId(), suspendDeviceModeId, suspendStatusId);
    }

    private void turnDeviceOn(Device item) {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(getActivity());
            return;
        }
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showNetworkError(getActivity());
            return;
        }
        String allowDeviceModeId = dm.getAllowDeviceModeId();
        String allowStatusId = dm == null ? null : dm.getAllowDeviceStatusId();
        if (allowDeviceModeId == null) {
            Toast.makeText(getActivity(), getString(R.string.device_modes_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        if (allowStatusId == null) {
            Toast.makeText(getActivity(), getString(R.string.device_statuses_not_loaded), Toast.LENGTH_LONG).show();
            return;
        }
        changeDeviceMode(item.getId(), allowDeviceModeId, allowStatusId);
    }

    private void changeDeviceMode(final String deviceId, final String modeId, final String statusId) {
        final ProgressDialog d = Utils.getProgressDialog(getActivity());
        Request request = new Request().add(new ChangeDeviceModeCommand(getActivity(), deviceId, modeId));
        Log.d(TAG, "ChangeDeviceMode request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(getActivity())) {

            @Override
            public void onPostExecute(String result) {
                d.dismiss();
                Log.d(TAG, "ChangeDeviceMode response: " + result);

                DeviceChange deviceChange = new DeviceChange(result);
                if (deviceChange.isOk()) {
                    Toast.makeText(getActivity(), getString(R.string.request_sent_to_device), Toast.LENGTH_LONG).show();
                    serviceConnector.getServiceBinder().waitForDeviceStatus(deviceId, statusId);
                } else {
                    Toast.makeText(getActivity(), deviceChange.getMessage(), Toast.LENGTH_LONG).show();
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
    public void onDestroyView() {
        serviceConnector.unbind();
        if (serviceConnector.getServiceBinder()!=null)
            serviceConnector.getServiceBinder().deactivateDeviceRequests();
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.inflater = inflater;
        view = inflater.inflate(R.layout.fragment_devices, container, false);

        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.action_devices));

        serviceConnector.bind(getActivity(), new Runnable() {
            @Override
            public void run() {
                dm = serviceConnector.getServiceBinder().getDataModel();
                serviceConnector.getServiceBinder().activateDeviceRequests();
                refreshDevices();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(SteagleService.Dictionary.DEV_MODE,
                        SteagleService.Dictionary.DEV_MODE_SRC, SteagleService.Dictionary.DEVICE,
                        SteagleService.Dictionary.DEVICE_STATUS, SteagleService.Dictionary.DEVICE_STATUS_CHANGE))) {
                    refreshDevices();
                }
            }
        };
        showConnectingView();
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        return view;
    }

}