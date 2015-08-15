package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class DeleteSensorCommand extends AccountCommand {

    private String sensorId;

    public DeleteSensorCommand(Context context, String sensorId) {
        super(context);
        this.sensorId = sensorId;
    }

    @Override
    protected String getRootTagName() {
        return "sensor";
    }

    @Override
    protected String getCommandName() {
        return "del";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_sensor", sensorId);
    }

}
