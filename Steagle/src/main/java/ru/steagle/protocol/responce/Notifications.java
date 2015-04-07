package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Notification;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 15.02.14.
 */
public class Notifications extends BaseResult {
    private static final String TAG = Notifications.class.getName();

    private List<Notification> list = new ArrayList<>();

    public Notifications(String xml) {
        parse(xml);
    }

    public List<Notification> getList() {
        return list;
    }

    public Notification getNewInstance(){
        return new Notification();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("user");
        var.setStartElementListener(new StartElementListener() {
            public void start(Attributes attrib) {
                readBaseFields(attrib);
            }
        });
        Element row = var.getChild("data").getChild("row");
        row.setStartElementListener(new StartElementListener() {
            public void start(Attributes attrib) {
                list.add(getNewInstance());
            }
        });
        row.getChild("date").setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String s) {
                //Log.d(TAG, "parse date: " + s);
                list.get(list.size() - 1).setDate(ISO8601.parse(s).getTime());
            }
        });
        row.getChild("raw").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setText(s);
            }
        });
        row.getChild("id_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDeviceId(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
