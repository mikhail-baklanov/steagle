package ru.steagle.protocol.request;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

import ru.steagle.config.Keys;

/**
 * Created by bmw on 16.02.14.
 */
abstract public class AccountCommand extends Command {
    protected Context context;

    AccountCommand(Context context) {
        this.context = context;
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String prefLogin = prefs.getString(Keys.LOGIN.getPrefKey(), null);
        String prefPassword = prefs.getString(Keys.PASSWORD.getPrefKey(), null);
        serializer.attribute("", "account", prefLogin);
        serializer.attribute("", "pwd_user", prefPassword);
    }

}
