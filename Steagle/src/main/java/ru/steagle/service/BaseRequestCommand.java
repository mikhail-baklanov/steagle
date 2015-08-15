package ru.steagle.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import ru.steagle.config.Keys;
import ru.steagle.datamodel.DataModel;

public abstract class BaseRequestCommand implements IRequestDataCommand {

    protected boolean canRun = true;
    protected long time2load;

    @Override
    public boolean isTerminated() {
        return false;
    }

}
