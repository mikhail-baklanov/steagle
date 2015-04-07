package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Tarif;

/**
 * Created by bmw on 15.02.14.
 */
public class Tarifs extends BaseResult {

    private List<Tarif> list = new ArrayList<>();

    public Tarifs(String xml) {
        parse(xml);
    }

    public List<Tarif> getList() {
        return list;
    }

    public Tarif getNewInstance(){
        return new Tarif();
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
        row.getChild("id_tarif").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setId(s);
            }
        });
        row.getChild("descr_tarif").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDescription(s);
            }
        });
        row.getChild("pm_summ").setEndTextElementListener(new EndTextElementListener() {
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
