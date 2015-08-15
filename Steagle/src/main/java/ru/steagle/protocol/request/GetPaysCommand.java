package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 16.02.14.
 */
public class GetPaysCommand extends AccountCommand {

    private Date b;
    private Date e;

    public GetPaysCommand(Context context, Date b, Date e) {
        super(context);
        this.b = b;
        this.e = e;
    }

    @Override
    protected String getRootTagName() {
        return "stat";
    }

    @Override
    protected String getCommandName() {
        return "fetch";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);

        if (b != null && e != null) {
            Calendar c = GregorianCalendar.getInstance();
            c.setTime(b);
            serializer.attribute("", "date_b", ISO8601.format(c));
            c = GregorianCalendar.getInstance();
            c.setTime(e);
            serializer.attribute("", "date_e", ISO8601.format(c));
        }
    }
}
