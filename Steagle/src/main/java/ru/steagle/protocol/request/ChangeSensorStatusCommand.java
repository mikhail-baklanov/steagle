package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeSensorStatusCommand extends AccountCommand {

    private String statusId;
    private String sensorId;

    public ChangeSensorStatusCommand(Context context, String sensorId, String statusId) {
        super(context);
        this.sensorId = sensorId;
        this.statusId = statusId;
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
        serializer.attribute("", "id_status_sensor", statusId);
    }

}
