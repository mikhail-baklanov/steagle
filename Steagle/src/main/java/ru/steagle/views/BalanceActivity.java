package ru.steagle.views;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import ru.steagle.config.Config;
import ru.steagle.R;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.GetPaysCommand;
import ru.steagle.datamodel.Pay;
import ru.steagle.protocol.responce.Pays;
import ru.steagle.service.SteagleService;

/**
 * Created by bmw on 09.02.14.
 */
public class BalanceActivity extends Activity {
    private final static String TAG = BalanceActivity.class.getName();
    private BroadcastReceiver broadcastReceiver;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);
    private Date startDate;
    private Date endDate;
    private List<Pay> list;

    private void refreshBalanceData() {
        LinearLayout layout;
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SimpleDateFormat f = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat fe = new SimpleDateFormat("dd MMMM");
        layout = (LinearLayout) findViewById(R.id.balansContainer);
        layout.removeAllViews();
        if (list != null) {
            String currency = " ...";
            if (serviceConnector.getServiceBinder() != null) {
                String c = serviceConnector.getServiceBinder().getDataModel().getCurrency();
                if (c != null) {
                    currency = " " + c;
                }
            }
            Date prevDay = null;
            for (final Pay item : list) {
                View v;
                Date currentDate = item.getDate();
                Date currentDay = Utils.getDay(currentDate);
                if (!currentDay.equals(prevDay)) {
                    prevDay = currentDay;
                    v = inflater.inflate(R.layout.balans_header, layout, false);
                    ((TextView) v.findViewById(R.id.header)).setText(fe.format(currentDate).toUpperCase());
                    layout.addView(v);
                }
                v = inflater.inflate(R.layout.balans_row, layout, false);
                ((TextView) v.findViewById(R.id.time)).setText(f.format(currentDate));
                ((TextView) v.findViewById(R.id.message)).setText(item.getDescription());
                TextView sum = ((TextView) v.findViewById(R.id.sum));
                sum.setTextColor(getResources().getColor(!item.getSum().contains("-") ? R.color.balansSumP : R.color.balansSumN));
                sum.setText(item.getSum() + currency);
                layout.addView(v);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_balance);

        ((TextView)findViewById(R.id.title)).setText(getString(R.string.balans));
        ((TextView)findViewById(R.id.operation_text)).setText(getString(R.string.action_period));
        findViewById(R.id.operation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PeriodEditDialogFragment dialog = new PeriodEditDialogFragment(
                        getString(R.string.enter_report_period), startDate, endDate, getString(R.string.btnCreateReport), getString(R.id.btnCancel), new PeriodEditDialogFragment.Listener() {
                    @Override
                    public void onYesClick(Date startD, Date endD) {
                        startDate = startD;
                        endDate = endD;
                        requestBalance();
                    }
                });
                dialog.show(getFragmentManager(), null);

            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                if (Utils.isObjectInSet(object, EnumSet.of(SteagleService.Dictionary.CURRENCY)))
                {
                    refreshBalanceData();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        requestBalance();
        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                refreshBalanceData();
            }
        });
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroy();
    }

    private void requestBalance() {
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }
        final ProgressDialog progressDialog = Utils.getProgressDialog(this);
        Request request = new Request().add(new GetPaysCommand(this, startDate, endDate));
        Log.d(TAG, "GetPays request: " + request);
        RequestTask requestTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                progressDialog.dismiss();
                Log.d(TAG, "GetPays response: " + result);

                Pays pays = new Pays(result);
                if (pays.isOk()) {
                    list = pays.getList();
                    refreshBalanceData();
                } else {
                    Toast.makeText(BalanceActivity.this, pays.getMessage(), Toast.LENGTH_LONG).show();
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