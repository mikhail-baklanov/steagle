package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeDeviceNameCommand extends AccountCommand {

    private String deviceName;
    private String deviceId;

    public ChangeDeviceNameCommand(Context context, String deviceId, String deviceName) {
        super(context);
        this.deviceId = deviceId;
        this.deviceName = deviceName;
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
        serializer.attribute("", "descr_dev", deviceName);
    }

}
