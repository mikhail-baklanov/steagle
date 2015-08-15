package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

/**
 * Created by bmw on 15.02.14.
 */
public class SensorChange extends BaseResult {
    private static final String TAG = SensorChange.class.getName();

    public SensorChange(String xml) {
        parse(xml);
    }

    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("sensor");
        var.setStartElementListener(new StartElementListener() {
            public void start(Attributes attrib) {
                readBaseFields(attrib);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
