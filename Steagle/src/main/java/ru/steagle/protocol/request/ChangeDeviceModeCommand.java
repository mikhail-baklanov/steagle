package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeDeviceModeCommand extends AccountCommand {

    private String modeId;
    private String deviceId;

    public ChangeDeviceModeCommand(Context context, String deviceId, String modeId) {
        super(context);
        this.deviceId = deviceId;
        this.modeId = modeId;
    }

    @Override
    protected String getRootTagName() {
        return "dev";
    }

    @Override
    protected String getCommandName() {
        return "ex_mod";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "id_dev", deviceId);
        serializer.attribute("", "id_mode", modeId);
    }

}
