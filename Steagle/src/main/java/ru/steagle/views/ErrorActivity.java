package ru.steagle.views;

import android.app.Activity;
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
import android.widget.EditText;
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
public class ErrorActivity extends Activity {
    public final static String ERROR_PARAM = "deviceId";
    private final static String TAG = ErrorActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);

        String error = getIntent().getStringExtra(ERROR_PARAM);

        ((EditText) findViewById(R.id.error)).setText(error);

    }

}
