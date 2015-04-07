package ru.steagle.protocol.request;

import android.content.Context;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class AuthCommand extends Command {

    private String login;
    private String password;

    public AuthCommand(String login, String password) {
        this.login = login;
        this.password = password;
    }
    @Override
    protected String getRootTagName() {
        return "user";
    }

    @Override
    protected String getCommandName() {
        return "inf";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        serializer.attribute("", "account", login);
        serializer.attribute("", "pwd_user", password);
    }

}
