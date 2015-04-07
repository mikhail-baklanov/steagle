package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeTimeZoneCommand extends AccountCommand {

    private String timeZone;

    public ChangeTimeZoneCommand(Context context, String timeZone) {
        super(context);
        this.timeZone = timeZone;
    }

    @Override
    protected String getRootTagName() {
        return "user";
    }

    @Override
    protected String getCommandName() {
        return "mod";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "tz", timeZone);
    }

}
