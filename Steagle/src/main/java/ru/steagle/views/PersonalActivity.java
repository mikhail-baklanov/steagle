package ru.steagle.views;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.steagle.config.Config;
import ru.steagle.datamodel.DataModel;
import ru.steagle.R;
import ru.steagle.service.SteagleServiceConnector;
import ru.steagle.utils.Utils;
import ru.steagle.protocol.Request;
import ru.steagle.protocol.RequestTask;
import ru.steagle.protocol.request.ChangePersonalInfoCommand;
import ru.steagle.datamodel.UserInfo;

/**
 * Created by bmw on 09.02.14.
 */
public class PersonalActivity extends Activity {
    private final static String TAG = PersonalActivity.class.getName();
    private boolean viewMode = true;
    private BroadcastReceiver broadcastReceiver;
    private SteagleServiceConnector serviceConnector = new SteagleServiceConnector(TAG);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_personal_info);

        ((TextView)findViewById(R.id.title)).setText(getString(R.string.personalInfo));
		
        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
        findViewById(R.id.btnCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = !viewMode;
                changeButtons();
                setData();
            }
        });
        findViewById(R.id.btnEdit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewMode = !viewMode;
                changeButtons();
                findViewById(R.id.personal_phone).requestFocus();
            }
        });

        serviceConnector.bind(this, new Runnable() {
            @Override
            public void run() {
                setData();
                changeButtons();
            }
        });
        changeButtons();
        setData();
    }

    private void saveData() {
        if (serviceConnector.getServiceBinder() == null) {
            Utils.showServiceConnectionError(this);
            return;
        }
        if (!Utils.isNetworkAvailable(this)) {
            Utils.showNetworkError(this);
            return;
        }

        final String phone = ((EditText) findViewById(R.id.personal_phone)).getText().toString();
        final String email = ((EditText) findViewById(R.id.personal_email)).getText().toString();
        final ProgressDialog dialog = Utils.getProgressDialog(this);
        Request request = new Request().add(new ChangePersonalInfoCommand(this, phone, email));
        Log.d(TAG, "ChangePersonalInfo request: " + request);
        RequestTask authTask = new RequestTask(Config.getRegServer(this)) {

            @Override
            public void onPostExecute(String result) {
                dialog.dismiss();
                Log.d(TAG, "ChangePersonalInfo response: " + result);

                UserInfo userInfo = new UserInfo(result);
                if (userInfo.isOk()) {
                    DataModel dm = serviceConnector.getServiceBinder().getDataModel();
                    dm.setPhone(phone);
                    dm.setEmail(email);
                    setData();
                    viewMode = !viewMode;
                    changeButtons();

                } else {
                    Toast.makeText(PersonalActivity.this, userInfo.getMessage(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onPreExecute() {
                dialog.show();
            }

        };
        authTask.execute(request.serialize());

    }

    @Override
    public void onDestroy() {
        serviceConnector.unbind();
        super.onDestroy();
    }

    private void setData() {
        String phone = "";
        String email = "";
        if (serviceConnector.getServiceBinder() != null) {
            DataModel dm = serviceConnector.getServiceBinder().getDataModel();
            phone = dm.getPhone();
            email = dm.getEmail();
        }
        ((EditText) findViewById(R.id.personal_phone)).setText(phone);
        ((EditText) findViewById(R.id.personal_email)).setText(email);
    }

    private void changeButtons() {
        findViewById(R.id.layoutView).setVisibility(
                viewMode && serviceConnector.getServiceBinder() != null ? View.VISIBLE : View.GONE);
        findViewById(R.id.layoutEdit).setVisibility(viewMode ? View.GONE : View.VISIBLE);

        boolean enabled = !viewMode && serviceConnector.getServiceBinder() != null;
        findViewById(R.id.personal_phone).setEnabled(enabled);
        findViewById(R.id.personal_email).setEnabled(enabled);
    }


}