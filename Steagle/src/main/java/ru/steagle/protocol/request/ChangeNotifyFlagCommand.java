package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangeNotifyFlagCommand extends AccountCommand {
    public static final String PHONE = "deny_avphone";
    public static final String EMAIL = "deny_aemail";
    public static final String SMS = "deny_asms";

    private String flag;
    private boolean enable;

    public ChangeNotifyFlagCommand(Context context, String flag, boolean enable) {
        super(context);
        this.flag = flag;
        this.enable = enable;
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
        serializer.attribute("", "flags_users", flag);
        serializer.attribute("", "val_flags_users", enable ? "0" : "1");
    }

}
