package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Pay;
import ru.steagle.utils.ISO8601;

/**
 * Created by bmw on 15.02.14.
 */
public class Pays extends BaseResult {
    private static final String TAG = Pays.class.getName();

    private List<Pay> list = new ArrayList<>();

    public Pays(String xml) {
        parse(xml);
    }

    public List<Pay> getList() {
        return list;
    }

    public Pay getNewInstance(){
        return new Pay();
    }
    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element var = root.getChild("stat");
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
        row.getChild("descr_cftt").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDescription(s);
            }
        });
        row.getChild("summ").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setSum(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
