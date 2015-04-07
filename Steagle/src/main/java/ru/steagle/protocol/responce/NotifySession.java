package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

public class NotifySession extends BaseResult {
    private String sessionId;

    public NotifySession(String xml) {
        parse(xml);
    }

    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element tag = root.getChild("user");
        tag.setStartElementListener(new StartElementListener() {
            public void start(Attributes attrib) {
                readBaseFields(attrib);
                sessionId = attrib.getValue("key_sess");
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }

    public String getSessionId() {
        return sessionId;
    }
}

