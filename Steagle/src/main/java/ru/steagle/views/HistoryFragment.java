package ru.steagle.views;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.R;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.GetEventsCommand;
import ru.steagle.datamodel.Event;
import ru.steagle.protocol.responce.Events;
import ru.steagle.service.SteagleService;

import static ru.steagle.service.SteagleService.Dictionary.DEVICE;
import static ru.steagle.service.SteagleService.Dictionary.DEV_MODE_SRC;
import static ru.steagle.service.SteagleService.Dictionary.LEVEL;
import static ru.steagle.service.SteagleService.Dictionary.SENSOR;

/**
 * Created by bmw on 09.02.14.
 */
public class HistoryFragment extends Fragment {
    private final static String TAG = HistoryFragment.class.getName();
    private View view;
    private BroadcastReceiver broadcastReceiver;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private String devId;
    private Date startDate;
    private Date endDate;
    private List<Event> list;
    private List<Event> sourceList;
    TextView tvImportant;
    TextView tvAll;
    private boolean showImportant = true;

    private void refreshEventsData() {
        filterList();
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat fe = new SimpleDateFormat("dd MMMM");
        layout = (LinearLayout) view.findViewById(R.id.container);
        layout.removeAllViews();
        if (list != null) {
            DataModel dm = null;
            if (serviceConnector.getServiceBinder() != null) {
                dm = serviceConnector.getServiceBinder().getDataModel();
            }

            Date prevDay = null;
            for (final Event item : list) {
                View v;
                Date currentDate = item.getDate();
                Date currentDay = Utils.getDay(currentDate);
                if (!currentDay.equals(prevDay)) {
                    prevDay = currentDay;
                    v = inflater.inflate(R.layout.history_header, layout, false);
                    ((TextView) v.findViewById(R.id.header)).setText(fe.format(currentDate).toUpperCase());
                    layout.addView(v);
                }
                v = inflater.inflate(R.layout.history_row, layout, false);
                ((TextView) v.findViewById(R.id.time)).setText(f.format(currentDate));
                String sensor;
                String mode;
                if (dm == null) {
                    sensor = "...";
                    mode = "...";
                } else {
                    sensor = dm.getSensorDescription(item.getSensorId());
                    if (sensor == null) {
                        sensor = item.getSensorId() == null ? null : "...";
                    }
                    String source = dm.getDevModeSrcDescription(item.getDevModeSrcId());
                    if (sensor == null) {
                        if (source == null) {
                            sensor = "";
                        } else {
                            sensor = source;
                        }
                    } else {
                        // sensor != null
                        if (source == null) {
                            if (item.getDevModeSrcId() != null)
                                sensor += " - " + "...";
                        } else
                            sensor += " - " + source;
                    }
                    String predMode = dm.getDevModeDescription(item.getDevModeId());
                    if (predMode == null) {
                        predMode = item.getDevModeId() == null ? "x" : "...";
                    }
                    String curMode = dm.getDevModeDescription(item.getDevLastModeId());
                    if (curMode == null) {
                        curMode = item.getDevLastModeId() == null ? "x" : "...";
                    }
                    mode = predMode + " -> " + curMode;
                }
                ((TextView) v.findViewById(R.id.source)).setText(sensor);
                ((TextView) v.findViewById(R.id.mode)).setText(mode);
                layout.addView(v);
            }

        }
    }

    private void filterList() {
        String importantLevelId = null;
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = null;
            dm = serviceConnector.getServiceBinder().getDataModel();
            if (dm != null)
                importantLevelId = dm.getImportantLevelId();
        }
        list = new ArrayList<>();
        if (sourceList == null) {
            return;
        }
        for (Event e : sourceList) {
            if (e != null) {
                if (devId == null || devId.equals(e.getDevId())) {
                    if (showImportant) {
                        if (importantLevelId != null && importantLevelId.equals(e.getLevelId()))
                            list.add(e);
                    } else
                        list.add(e);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_history, container, false);

        ((TextView)view.findViewById(R.id.title)).setText(getString(R.string.action_history));
        ((TextView)view.findViewById(R.id.operation_text)).setText(getString(R.string.action_filter));
        view.findViewById(R.id.operation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PeriodAndDevEditDialogFragment dialog = new PeriodAndDevEditDialogFragment(
                        getString(R.string.enter_filter), devId, startDate, endDate, getString(R.string.btnFilter), getString(R.id.btnCancel), new PeriodAndDevEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String id, Date startD, Date endD) {
                        devId = id;
                        startDate = startD;
                        endDate = endD;
                        requestEvents();
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });

        tvAll = (TextView) view.findViewById(R.id.tvAll);
        tvImportant = (TextView) view.findViewById(R.id.tvImportant);
        tvAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportant = false;
                showImportantAndAllSwitch();
                refreshEventsData();
            }
        });
        tvImportant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImportant = true;
                showImportantAndAllSwitch();
                refreshEventsData();
            }
        });
//        view.findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                PeriodAndDevEditDialogFragment dialog = new PeriodAndDevEditDialogFragment(
//                        getString(R.string.enter_filter), devId, startDate, endDate, getString(R.string.btnFilter), getString(R.id.btnCancel), new PeriodAndDevEditDialogFragment.Listener() {
//                    @Override
//                    public void onYesClick(String id, Date startD, Date endD) {
//                        devId = id;
//                        startDate = startD;
//                        endDate = endD;
//                        requestEvents();
//                    }
//                });
//                dialog.show(getFragmentManager(), null);
//
//            }
//        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(SENSOR, DEVICE, LEVEL, DEV_MODE_SRC))) {
                    refreshEventsData();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        requestEvents();
        serviceConnector.bind(getActivity(), new Runnable() {
            @Override
            public void run() {
                refreshEventsData();
            }
        });
        showImportantAndAllSwitch();
        return view;
    }

    private void showImportantAndAllSwitch() {
        if (showImportant) {
            tvImportant.setBackgroundColor(getResources().getColor(R.color.historySwitchSelectedColor));
            tvImportant.setTextColor(getResources().getColor(R.color.historySwitchSelectedTextColor));
            tvAll.setBackgroundColor(getResources().getColor(R.color.historySwitchUnselectedColor));
            tvAll.setTextColor(getResources().getColor(R.color.historySwitchUnselectedTextColor));
        } else {
            tvAll.setBackgroundColor(getResources().getColor(R.color.historySwitchSelectedColor));
            tvAll.setTextColor(getResources().getColor(R.color.historySwitchSelectedTextColor));
            tvImportant.setBackgroundColor(getResources().getColor(R.color.historySwitchUnselectedColor));
            tvImportant.setTextColor(getResources().getColor(R.color.historySwitchUnselectedTextColor));
        }
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroyView();
    }

    private void requestEvents() {
        if (!Utils.isNetworkAvailable(getActivity())) {
            Utils.showNetworkError(getActivity());
            return;
        }
        final ProgressDialog progressDialog = Utils.getProgressDialog(getActivity());
        Request request = new Request().add(new GetEventsCommand(getActivity(), startDate, endDate));
        Log.d(TAG, "GetEvents request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(getActivity())) {

            @Override
            public void onPostExecute(String result) {
                progressDialog.dismiss();
                Log.d(TAG, "GetEvents response: " + result);

                Events events = new Events(result);
                if (events.isOk()) {
                    sourceList = events.getList();
                    refreshEventsData();
                } else {
                    Toast.makeText(getActivity(), events.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                progressDialog.show();
            }

        };
        requestTask.execute(request.serialize());
    }
}