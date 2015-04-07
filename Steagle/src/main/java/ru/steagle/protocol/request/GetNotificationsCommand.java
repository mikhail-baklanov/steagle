package ru.steagle.protocol.request;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

/**
 * Created by bmw on 16.02.14.
 */
public class GetNotificationsCommand extends Command {

    private String sessionId;

    public GetNotificationsCommand(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    protected String getRootTagName() {
        return "user";
    }

    @Override
    protected String getCommandName() {
        return "chk_msg";
    }

    @Override
    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
        super.serializePrivateAttributes(serializer);
        serializer.attribute("", "key_sess", sessionId);
    }

}
