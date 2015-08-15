package ru.steagle.views;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.steagle.R;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Event;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.DescendPeriodScanner;
import ru.steagle.utils.Utils;

import static ru.steagle.service.SteagleService.Dictionary.DEVICE;
import static ru.steagle.service.SteagleService.Dictionary.DEV_MODE_SRC;
import static ru.steagle.service.SteagleService.Dictionary.HISTORY;
import static ru.steagle.service.SteagleService.Dictionary.LEVEL;
import static ru.steagle.service.SteagleService.Dictionary.SENSOR;

/**
 * Created by bmw on 09.02.14.
 */
public class HistoryFragment extends ListFragment implements AbsListView.OnScrollListener {
    private final static String TAG = HistoryFragment.class.getName();
    private View view;
    private BroadcastReceiver broadcastReceiver;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private String devId;
    private Date startFilterDate;
    private Date endFilterDate;
    private String importantLevelId;
    private HistoryAdapter adapter;
    private Integer requestId;
    SimpleDateFormat f = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private DescendPeriodScanner periodScanner = new DescendPeriodScanner();
    private ImportantSwitcher importantSwitcher = new ImportantSwitcher();
    private View loadingView;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setListAdapter(null);
        getListView().addFooterView(loadingView, null, false);
        adapter = new HistoryAdapter(getActivity());
        setListAdapter(adapter);
        getListView().setOnScrollListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_history, container, false);

        loadingView = view.inflate(getActivity(), R.layout.history_loading, null);

        ((TextView) view.findViewById(R.id.title)).setText(getString(R.string.action_history));
        ((TextView) view.findViewById(R.id.operation_text)).setText(getString(R.string.action_filter));
        view.findViewById(R.id.operation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PeriodAndDevEditDialogFragment dialog = new PeriodAndDevEditDialogFragment(
                        getString(R.string.enter_filter), devId, startFilterDate, endFilterDate, getString(R.string.btnFilter), getString(R.string.btnCancel), new PeriodAndDevEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(String id, Date startD, Date endD) {
                        devId = id;
                        startFilterDate = startD;
                        endFilterDate = endD;
                        periodScanner.setPeriod(startD, endD);
                        adapter.cleatEvents();
                        requestEvents();
                    }
                });
                dialog.show(getFragmentManager(), null);
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (object == null)
                    return;
                if (Utils.isObjectInSet(object, EnumSet.of(SENSOR, DEVICE, LEVEL, DEV_MODE_SRC))) {
                    updateImportantLevelId();
                    requestEvents();
                } else if (object.equals(HISTORY.toString())) {
                    int requestId = intent.getIntExtra(SteagleService.HISTORY_REQUEST_ID, -1);
                    if (requestId >= 0 && HistoryFragment.this.requestId != null && requestId == HistoryFragment.this.requestId.intValue()) {
                        HistoryFragment.this.requestId = null;
                        List<Event> events = serviceConnector.getServiceBinder().getHistoryEvents();
                        if (events != null && events.size() > 0) {
                            periodScanner.setPointer(events.get(events.size() - 1).getDate());
                            adapter.addEvents(events);
                            loadingView.setVisibility(View.VISIBLE);
                        } else {
                            periodScanner.finish();
                            loadingView.setVisibility(View.GONE);
                        }
                        //Toast.makeText(getActivity(), "get response. set current date to " + f.format(currentDate), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        serviceConnector.bind(getActivity(), new Runnable() {
            @Override
            public void run() {
                updateImportantLevelId();
                adapter.setDataModel(serviceConnector.getServiceBinder().getDataModel());
                requestEvents();
            }
        });
        periodScanner.setPeriod(startFilterDate, endFilterDate);
        importantSwitcher.reset(getActivity(), view, new Runnable() {

            @Override
            public void run() {
                periodScanner.resetPointer();
                adapter.cleatEvents();
                requestEvents();
            }
        }
        );

        return view;
    }

    private void updateImportantLevelId() {
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = serviceConnector.getServiceBinder().getDataModel();
            if (dm != null)
                importantLevelId = dm.getImportantLevelId();
        }
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroy();
    }

    private void requestEvents() {

        if (serviceConnector.getServiceBinder() == null) {
            return;
        }
        if (requestId != null)
            return;

        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(mi);
            long availableMemory = mi.availMem;
            Log.d(TAG, "Free memory: " + availableMemory);
            if (availableMemory < 10000000L) { // 10 миллионов
                Toast.makeText(getActivity(), R.string.not_enought_memory, Toast.LENGTH_LONG).show();
                periodScanner.finish();
                loadingView.setVisibility(View.GONE);
                return;
            }
        }

        String level = importantSwitcher.isImportantSwitch() ? importantLevelId : null;

        //Toast.makeText(getActivity(), f.format(startDate) + "-" + f.format(currentDate) + "/" + level, Toast.LENGTH_LONG).show();

        requestId = serviceConnector.getServiceBinder().createHistoryRequest(periodScanner.getUnscannedStartDate(), periodScanner.getUnscannedEndDate(), level);

    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {

    }

    @Override
    public void onScroll(AbsListView view,
                         int firstVisible, int visibleCount, int totalCount) {
        if (periodScanner.isUnscannedEmpty())
            return;

        boolean loadMore = /* maybe add a padding */
                firstVisible + visibleCount >= totalCount;

        if (loadMore) {
            requestEvents();
        }

    }
}