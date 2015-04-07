package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Device;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 15.02.14.
 */
public class Devices extends BaseResult {
    private static final String TAG = Devices.class.getName();

    private List<Device> list = new ArrayList<>();

    public Devices(String xml) {
        parse(xml);
    }

    public List<Device> getList() {
        return list;
    }

    public Device getNewInstance(){
        return new Device();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("dev");
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
        row.getChild("id_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setId(s);
            }
        });
        row.getChild("id_type_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setTypeId(s);
            }
        });
        row.getChild("key_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setKey(s);
            }
        });
        row.getChild("last_id_dev_mode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setLastModeId(s);
            }
        });
        row.getChild("id_status_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setStatusId(s);
            }
        });
        row.getChild("id_dev_state").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setStateId(s);
            }
        });
        row.getChild("descr_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDescription(s);
            }
        });
        row.getChild("date_last_id_dev_mode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                //Log.d(TAG, "parse date: " + s);
                list.get(list.size() - 1).setDateLastModeId(ISO8601.parse(s).getTime());
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
