package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeMasterPasswordCommand extends AccountCommand {

    private String password;
    private String deviceId;

    public ChangeMasterPasswordCommand(Context context, String deviceId, String password) {
        super(context);
        this.deviceId = deviceId;
        this.password = password;
    }

    @Override
    protected String getRootTagName() {
        return "dev";
    }

    @Override
    protected String getCommandName() {
        return "mod";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_dev", deviceId);
        serializer.attribute("", "dev_pwd", password);
    }

}
