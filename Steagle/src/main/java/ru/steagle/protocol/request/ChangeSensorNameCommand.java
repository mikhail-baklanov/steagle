package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeSensorNameCommand extends AccountCommand {

    private String sensorName;
    private String sensorId;

    public ChangeSensorNameCommand(Context context, String sensorId, String sensorName) {
        super(context);
        this.sensorId = sensorId;
        this.sensorName = sensorName;
    }

    @Override
    protected String getRootTagName() {
        return "sensor";
    }

    @Override
    protected String getCommandName() {
        return "mod";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_sensor", sensorId);
        serializer.attribute("", "descr_sensor", sensorName);
    }

}
