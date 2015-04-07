package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.DevModeSrc;

/**
 * Created by bmw on 15.02.14.
 */
public class DevModeSrcs extends BaseResult {

    private List<DevModeSrc> list = new ArrayList<>();

    public DevModeSrcs(String xml) {
        parse(xml);
    }

    public List<DevModeSrc> getList() {
        return list;
    }

    public DevModeSrc getNewInstance(){
        return new DevModeSrc();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("var");
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
        row.getChild("id_dev_mode_src").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setId(s);
            }
        });
        row.getChild("key").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setKey(s);
            }
        });
        row.getChild("descr_dev_mode_src").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDescription(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
