package ru.steagle.datamodel;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.protocol.responce.BaseResult;

/**
 * Created by bmw on 15.02.14.
 */
abstract public class SimpleDictionary<T extends SimpleDictionaryElem> extends BaseResult {

    private List<T> list = new ArrayList<>();

    public SimpleDictionary(String xml) {
        parse(xml);
    }

    public List<T> getList() {
        return list;
    }

    abstract public T getNewInstance();
    abstract public String getIdName();
    public String getDescriptionName() {
        return "descr";
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
        row.getChild(getIdName()).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setId(s);
            }
        });
        row.getChild(getDescriptionName()).setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setDescription(s);
            }
        });
        row.getChild("key").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size()-1).setKey(s);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }
}
