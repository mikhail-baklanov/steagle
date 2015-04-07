package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.List;

import ru.steagle.datamodel.UserInfo;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeNotifyCommand extends AccountCommand {

    public enum NOTIFY_OBJECT {
        PHONE, SMS, EMAIL
    }

    private String attribName;
    private String value;

    public ChangeNotifyCommand(Context context, NOTIFY_OBJECT notifyObject, List<String> values) {
        super(context);
        if (NOTIFY_OBJECT.PHONE.equals(notifyObject)) {
            attribName = UserInfo.NOTIFY_PHONES_ATTRIB;
        } else if (NOTIFY_OBJECT.EMAIL.equals(notifyObject)) {
            attribName = UserInfo.NOTIFY_EMAILS_ATTRIB;
        } else if (NOTIFY_OBJECT.SMS.equals(notifyObject)) {
            attribName = UserInfo.NOTIFY_SMS_ATTRIB;
        }
        value = "";
        for (String v : values) {
            if (value.length() > 0)
                value += ",";
            value += v;
        }
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
        serializer.attribute("", attribName, value);
    }

}
