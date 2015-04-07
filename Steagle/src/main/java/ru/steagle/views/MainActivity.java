package ru.steagle.views;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import ru.steagle.R;
import ru.steagle.SteagleApplication;

public class MainActivity extends ActionBarActivity
        implements MenuFragment.Listener, AccountFragment.Listener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SteagleApplication)getApplication()).bindSteagleService();
        setContentView(R.layout.activity_main);
        MenuFragment menuFragment = new MenuFragment();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.menuContainer, menuFragment);
        transaction.commit();

    }

    @Override
    protected void onDestroy() {
        ((SteagleApplication) getApplication()).unBindSteagleService();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAccountClick() {
        AccountFragment accountFragment = new AccountFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, accountFragment);
        transaction.commit();
        setTitle(getString(R.string.action_account));
    }

    @Override
    public void onDevicesClick() {
        DevicesFragment devicesFragment = new DevicesFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, devicesFragment);
        transaction.commit();
        setTitle(getString(R.string.action_devices));
    }

    @Override
    public void onHistoryClick() {
        HistoryFragment historyFragment = new HistoryFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, historyFragment);
        transaction.commit();
        setTitle(getString(R.string.action_history));
    }

    @Override
    public void onSettingsClick() {
        SettingsFragment settingsFragment = new SettingsFragment();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.contentContainer, settingsFragment);
        transaction.commit();
        setTitle(getString(R.string.settings));
    }

    @Override
    public void onNotificationsClick() {
        Intent intent = new Intent(this, NotificationsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBalanceClick() {
        Intent intent = new Intent(this, BalanceActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPersonalInfoClick() {
        Intent intent = new Intent(this, PersonalActivity.class);
        startActivity(intent);
    }

    @Override
    public void onTimeZonesClick() {
        Intent intent = new Intent(this, TimeZoneActivity.class);
        startActivity(intent);
    }

}
