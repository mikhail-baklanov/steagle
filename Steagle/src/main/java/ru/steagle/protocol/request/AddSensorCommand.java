package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class AddSensorCommand extends AccountCommand {

    private String deviceId;
    private String sensorTypeId;

    public AddSensorCommand(Context context, String deviceId, String sensorTypeId) {
        super(context);
        this.deviceId = deviceId;
        this.sensorTypeId = sensorTypeId;
    }

    @Override
    protected String getRootTagName() {
        return "sensor";
    }

    @Override
    protected String getCommandName() {
        return "add";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_dev", deviceId);
        serializer.attribute("", "id_type_sensor", sensorTypeId);
    }

}
