package ru.steagle.views;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.steagle.R;
import ru.steagle.datamodel.DataModel;
import ru.steagle.datamodel.Tarif;
import ru.steagle.datamodel.TimeZone;
import ru.steagle.service.SteagleService;
import ru.steagle.service.SteagleServiceConnector;

/**
 * Created by bmw on 08.02.14.
 */
public class AccountFragment extends Fragment {
    private static final String TAG = AccountFragment.class.getName();

    public interface Listener {
        void onNotificationsClick();
        void onBalanceClick();
        void onPersonalInfoClick();
        void onTimeZonesClick();
    }

    private BroadcastReceiver broadcastReceiver;
    private Listener listener;
    private View view;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);
        ((TextView)view.findViewById(R.id.title)).setText(getString(R.string.action_account));
        LinearLayout notificationsPanel = (LinearLayout) view.findViewById(R.id.notificationsItem);
        notificationsPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onNotificationsClick();

            }
        });
        LinearLayout balancePanel = (LinearLayout) view.findViewById(R.id.balansItem);
        balancePanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onBalanceClick();

            }
        });

        LinearLayout personalInfoPanel = (LinearLayout) view.findViewById(R.id.personalInfoItem);
        personalInfoPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onPersonalInfoClick();

            }
        });
        LinearLayout timeZonesPanel = (LinearLayout) view.findViewById(R.id.timeZoneItem);
        timeZonesPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null)
                    listener.onTimeZonesClick();

            }
        });

        serviceConnector.bind(getActivity(), new Runnable() {
            @Override
            public void run() {
                updateUI();
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                //String object = intent.getStringExtra(SteagleService.OBJECT_NAME);
                //if (SteagleService.Dictionary.USER_STATUS.toString().equals(object)) {
                    updateUI();
                //}
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(SteagleService.BROADCAST_ACTION));
        updateUI();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.listener = activity instanceof Listener ? (Listener) activity : null;
    }

    private void updateUI() {
        android.content.res.Resources resources = getActivity().getResources();
        String status = resources.getString(R.string.unknown_user_status);
        String userName = resources.getString(R.string.unknown_user_name);
        String balance = resources.getString(R.string.unknown_balance);
        String account = resources.getString(R.string.unknown_account);
        String defCurrency = resources.getString(R.string.unknown_currency);
        String currency = defCurrency;
        String timeZone = resources.getString(R.string.unknown_time_zone);
        String tarif = resources.getString(R.string.unknown_tarif);
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = serviceConnector.getServiceBinder().getDataModel();
            String s = dm.getUserStatus();
            if (s != null)
                status = s;
            userName = dm.getUserName();
            balance = dm.getBalance();
            account = dm.getAccount();

            /* 08.04.2014 Konstantin Yakubov:
               Дима в протоколе отдает номер  укр. аккаунта обрезанным, есть нюансы на стороне серверного ПО
               Аккаунт  0442212020 – у нас в строке  где номер счета в окне «аккаунт» отображается 442212020.
               Можешь, пожалуйста, «0» добавить в случае, если приходит от Димы 9 цифр. Если 10- то все ок.
             */
            if (account != null) {
                if (account.trim().length() == 9)
                    account = "0" + account.trim();
            }

            currency = dm.getCurrency();
            if (balance != null) {
                balance += " " + (currency == null ? defCurrency : currency);
            }
            TimeZone tz = dm.getTimeZone();
            if (tz != null) {
                timeZone = tz.getName();
            }
            Tarif t = dm.getTarif();
            if (t != null) {
                tarif = t.getDescription();
            }
        }
        ((TextView) view.findViewById(R.id.accountStatus)).setText(status);
        ((TextView) view.findViewById(R.id.owner)).setText(userName);
        ((TextView) view.findViewById(R.id.balance)).setText(balance);
        ((TextView) view.findViewById(R.id.account)).setText(account);
        ((TextView) view.findViewById(R.id.timeZone)).setText(timeZone);
        ((TextView) view.findViewById(R.id.tarif)).setText(tarif);
    }

    @Override
    public void onDestroyView() {
        getActivity().unregisterReceiver(broadcastReceiver);
        serviceConnector.unbind();
        super.onDestroyView();
    }

    @Override
    public void onDetach() {
        this.listener = null;
        super.onDetach();
    }
}