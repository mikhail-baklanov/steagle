package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class GetSensorsCommand extends AccountCommand {

    private String deviceId;

    public GetSensorsCommand(Context context, String deviceId) {
        super(context);
        this.deviceId = deviceId;
    }

    @Override
    protected String getRootTagName() {
        return "sensor";
    }

    @Override
    protected String getCommandName() {
        return "fetch";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_dev", deviceId);
    }

}
