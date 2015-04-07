package ru.steagle.protocol.request;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;

abstract public class Command {
    abstract protected String getRootTagName();

    abstract protected String getCommandName();

    public void serialize(XmlSerializer serializer, int commandId) throws IOException {
        serializer.startTag("", getRootTagName());
        if (commandId > 0)
            serializer.attribute("", "id", "" + commandId);
        serializer.attribute("", "cmd", getCommandName());
        serializePrivateAttributes(serializer);
        serializer.endTag("", getRootTagName());
    }

    protected void serializePrivateAttributes(XmlSerializer serializer) throws IOException {
    }

}
