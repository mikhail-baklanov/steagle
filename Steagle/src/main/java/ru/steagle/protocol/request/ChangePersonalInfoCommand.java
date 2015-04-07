package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class ChangePersonalInfoCommand extends AccountCommand {

    private String phone;
    private String email;

    public ChangePersonalInfoCommand(Context context, String phone, String email) {
        super(context);
        this.phone = phone;
        this.email = email;
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
        serializer.attribute("", "phone", phone);
        serializer.attribute("", "mail", email);
    }

}
