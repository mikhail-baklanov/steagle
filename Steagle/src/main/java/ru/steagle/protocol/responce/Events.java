package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Event;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 15.02.14.
 */
public class Events extends BaseResult {
    private static final String TAG = Events.class.getName();

    private List<Event> list = new ArrayList<>();

    public Events(String xml) {
        parse(xml);
    }

    public List<Event> getList() {
        return list;
    }

    public Event getNewInstance(){
        return new Event();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("event");
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
        row.getChild("pdate").setEndTextElementListener(new EndTextElementListener() {

            @Override
            public void end(String s) {
                //Log.d(TAG, "parse date: " + s);
                list.get(list.size() - 1).setDate(ISO8601.parse(s).getTime());
            }
        });
        row.getChild("level").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setLevelId(s);
            }
        });
        row.getChild("id_type_event").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setEventTypeId(s);
            }
        });
        row.getChild("id_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setSensorId(s);
            }
        });
        row.getChild("id_dev_mode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDevModeId(s);
            }
        });
        row.getChild("id_dev_lmode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDevLastModeId(s);
            }
        });
        row.getChild("id_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDevId(s);
            }
        });
        row.getChild("id_dev_mode_src").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDevModeSrcId(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
