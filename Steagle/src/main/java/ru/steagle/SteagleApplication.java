package ru.steagle;

import android.app.Application;

import ru.steagle.service.SteagleServiceConnector;

/**
 * Created by bmw on 10.02.14.
 */
public class SteagleApplication extends Application {

    private final static String TAG = SteagleApplication.class.getName();
    private SteagleServiceConnector connector = new SteagleServiceConnector(TAG);
    private int bindCounter;

    public void bindSteagleService() {
        if (bindCounter == 0)
            connector.bind(getBaseContext(), null);
        bindCounter++;
    }

    public void unBindSteagleService() {
        bindCounter--;
        if (bindCounter == 0)
            connector.unbind();
    }


}
