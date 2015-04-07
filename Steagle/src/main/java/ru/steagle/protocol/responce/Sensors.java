package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Sensor;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 15.02.14.
 */
public class Sensors extends BaseResult {
    private static final String TAG = Sensors.class.getName();

    private List<Sensor> list = new ArrayList<>();

    public Sensors(String xml) {
        parse(xml);
    }

    public List<Sensor> getList() {
        return list;
    }

    public Sensor getNewInstance(){
        return new Sensor();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("sensor");
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
        row.getChild("id_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setId(s);
            }
        });
        row.getChild("id_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDevId(s);
            }
        });
        row.getChild("id_type_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setTypeId(s);
            }
        });
        row.getChild("id_status_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setStatusId(s);
            }
        });
        row.getChild("low_b").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setLowBattery(s);
            }
        });
        row.getChild("descr_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDescription(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
