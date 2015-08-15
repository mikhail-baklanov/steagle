package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.steagle.config.Config;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 16.02.14.
 */
public class GetEventsCommand extends AccountCommand {

    private Date b;
    private Date e;
    private String level;

    public GetEventsCommand(Context context, Date b, Date e, String level) {
        super(context);
        this.b = b;
        this.e = e;
        this.level = level;
    }

    @Override
    protected String getRootTagName() {
        return "event";
    }

    @Override
    protected String getCommandName() {
        return "fetch1";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        if (b != null) {
            Calendar c = GregorianCalendar.getInstance();
            c.setTime(b);
            serializer.attribute("", "date_b", ISO8601.format(c));
        }
        if (e != null) {
            Calendar c = GregorianCalendar.getInstance();
            c.setTime(e);
            serializer.attribute("", "date_e", ISO8601.format(c));
        }
        if (level != null) {
            serializer.attribute("", "level", level);
        }
        serializer.attribute("", "limit", "100");
    }
}
