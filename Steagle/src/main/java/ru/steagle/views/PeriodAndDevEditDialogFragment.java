package ru.steagle.views;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.List;

import ru.steagle.datamodel.DataModel;
import ru.steagle.R;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;
import ru.steagle.datamodel.Device;
import ru.steagle.service.SteagleService;

import static ru.steagle.service.SteagleService.Dictionary.DEVICE;

/**
 * Created by bmw on 09.02.14.
 */
public class PeriodAndDevEditDialogFragment extends DialogFragment {
    private static final String TAG = PeriodAndDevEditDialogFragment.class.getName();

    public interface Listener {
        void onYesClick(String devId, Date startDate, Date endDate);
    }

    private BroadcastReceiver broadcastReceiver;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private Listener listener;
    private View view;
    private String devId;
    private String title;
    private Date startDate;
    private Date endDate;
    private String yesButtonText;
    private String noButtonText;
    private TextView tvStartDate;
    private TextView tvEndDate;
    private Spinner spiDevList;
    private List<Device> deviceList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_period_and_dev, container, false);
        spiDevList = (Spinner) view.findViewById(R.id.devList);
        tvStartDate = (TextView) view.findViewById(R.id.period_start);
        tvEndDate = (TextView) view.findViewById(R.id.period_end);
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(DEVICE))) {
                    fillDevList();
                }
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        serviceConnector.bind(getActivity(), new Runnable() {
            @Override
            public void run() {
                fillDevList();
            }
        });
        return view;
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(title);
        fillDevList();
        updateDateFields();
        tvStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = GregorianCalendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        startDate = c.getTime();
                        updateDateFields();
                    }
                };
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(startDate == null ? new Date() : startDate);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), myCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();

            }
        });
        tvEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog.OnDateSetListener myCallBack = new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        Calendar c = GregorianCalendar.getInstance();
                        c.set(year, monthOfYear, dayOfMonth);
                        endDate = c.getTime();
                        updateDateFields();
                    }
                };
                Calendar c = GregorianCalendar.getInstance();
                c.setTime(endDate == null ? new Date() : endDate);
                DatePickerDialog dialog = new DatePickerDialog(getActivity(), myCallBack, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });
        ((Button) (view.findViewById(R.id.btnYes))).setText(yesButtonText);
        view.findViewById(R.id.btnYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onYesClick(devId, startDate, endDate);
                getDialog().cancel();
            }
        });
        ((Button) (view.findViewById(R.id.btnNo))).setText(noButtonText);
        view.findViewById(R.id.btnNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().cancel();
            }
        });

    }

    private void fillDevList() {
        deviceList = new ArrayList<>();
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = serviceConnector.getServiceBinder().getDataModel();
            if (dm.getDevices() != null)
                deviceList = dm.getDevices();
        }
        List<String> data = new ArrayList<>();
        int position = -1;
        int i = 0;
        for (Device d : deviceList) {
            if (devId != null && devId.equals(d.getId()))
                position = i;
            data.add(d.getDescription());
            i++;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spiDevList.setAdapter(adapter);
        spiDevList.setPrompt(getString(R.string.chooseDevice));
        if (position >= 0)
            spiDevList.setSelection(position);
        spiDevList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                devId = deviceList.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    private void updateDateFields() {
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
        tvStartDate.setText(
                startDate == null ? getActivity().getResources().getString(R.string.empty_date) : df.format(startDate));
        tvEndDate.setText(
                endDate == null ? getActivity().getResources().getString(R.string.empty_date) : df.format(endDate));
    }

    public PeriodAndDevEditDialogFragment(String title, String devId, Date startDate, Date endDate, String yesButtonText, String noButtonText, Listener listener) {
        this.title = title;
        this.devId = devId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.yesButtonText = yesButtonText;
        this.noButtonText = noButtonText;
        this.listener = listener;
    }

}