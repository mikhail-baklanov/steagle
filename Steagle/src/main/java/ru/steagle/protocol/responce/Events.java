package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.support.v7.appcompat.R;
import android.util.Xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.Event;
import ru.steagle.utils.ISO8601;
import ru.steagle.utils.Utils;

/**
 * Created by bmw on 15.02.14.
 */
public class Events extends BaseResult {
    private static final String TAG = Events.class.getName();

    private static final String RES = " res=\"";
    private static final String MSG = " msg=\"";
    private static final String ROW_END = "</row>";
    private static final String ROW_START = "<row>";

    private List<Event> list = new ArrayList<>();
    private boolean newParseMethod = true;

    public Events(String xml) {
        parse(xml);
    }

    public List<Event> getList() {
        return list;
    }

    public Event getNewInstance() {
        return new Event();
    }

    private void parseOld(String xml) throws SAXException {
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
                list.get(list.size() - 1).setLevelId(s);
            }
        });
        row.getChild("id_type_event").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setEventTypeId(s);
            }
        });
        row.getChild("id_sensor").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setSensorId(s);
            }
        });
        row.getChild("id_dev_mode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDevModeId(s);
            }
        });
        row.getChild("id_dev_lmode").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDevLastModeId(s);
            }
        });
        row.getChild("id_dev").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDevId(s);
            }
        });
        row.getChild("id_dev_mode_src").setEndTextElementListener(new EndTextElementListener() {
            @Override
            public void end(String s) {
                list.get(list.size() - 1).setDevModeSrcId(s);
            }
        });
        Xml.parse(xml, root.getContentHandler());
    }

    private void parseNew(String xml) throws Exception {
        if (xml == null) {
            throw new Exception("пустое тело ответа");
        }
        // парсинг кода ответа success/fail
        // <event cmd="fetch" res="success">
        int p1 = xml.indexOf("<event ");
        if (p1 < 0)
            throw new Exception("не обнаружен код ответа сервера");
        int p2 = xml.indexOf(">", p1);
        if (p2 < 0)
            throw new Exception("не обнаружен код ответа сервера");
        String s = xml.substring(p1, p2);
        int p = p2;
        p1 = s.indexOf(RES);
        if (p1 < 0)
            throw new Exception("не обнаружен код ответа сервера");
        p2 = s.indexOf("\"", p1 + RES.length());
        if (p2 < 0)
            throw new Exception("не обнаружен код ответа сервера");

        boolean ok = "success".equals(s.substring(p1 + RES.length(), p2));
        setOk(ok);
        if (!ok) {
            p1 = s.indexOf(MSG);
            if (p1 < 0)
                throw new Exception("не обнаружено сообщение от сервера");
            p2 = s.indexOf("\"", p1 + MSG.length());
            if (p2 < 0)
                throw new Exception("не обнаружено сообщение от сервера");

            String msg = s.substring(p1 + MSG.length(), p2);
            setMessage(msg);
            return;
        }
        // парсинг тела ответа
        // <row>...</row>

        String dateSTag = "<pdate>";
        String dateETag = "</pdate>";
        String levelSTag = "<level>";
        String levelETag = "</level>";
        String typeSTag = "<id_type_event>";
        String typeETag = "</id_type_event>";
        String sensorSTag = "<id_sensor>";
        String sensorETag = "</id_sensor>";
        String devModeSTag = "<id_dev_mode>";
        String devModeETag = "</id_dev_mode>";
        String devLModeSTag = "<id_dev_lmode>";
        String devLModeETag = "</id_dev_lmode>";
        String devSTag = "<id_dev>";
        String devETag = "</id_dev>";
        String devModeSrcSTag = "<id_dev_mode_src>";
        String devModeSrcETag = "</id_dev_mode_src>";

        while (true) {
            p1 = xml.indexOf(ROW_START, p);
            if (p1 < 0)
                break;
            p2 = xml.indexOf(ROW_END, p1 + ROW_START.length());
            if (p2 < 0)
                throw new Exception("не обнаружено завершение тега <row>");
            p = p2 + ROW_END.length();
            s = xml.substring(p1 + ROW_START.length(), p2);
            Event event = getNewInstance();

            event.setDate(ISO8601.parse(getTag(s, dateSTag, dateETag)).getTime());
            event.setLevelId(getTag(s, levelSTag, levelSTag));
            event.setEventTypeId(getTag(s, typeSTag, typeETag));
            event.setSensorId(getTag(s, sensorSTag, sensorETag));
            event.setDevModeId(getTag(s, devModeSTag, devModeETag));
            event.setDevLastModeId(getTag(s, devLModeSTag, devLModeETag));
            event.setDevId(getTag(s, devSTag, devETag));
            event.setDevModeSrcId(getTag(s, devModeSrcSTag, devModeSrcETag));

            list.add(event);
        }
    }

    private String getTag(String s, String sTag, String eTag) {
        int p1 = s.indexOf(sTag);
        if (p1 < 0)
            return null;
        int fromIndex = p1 + sTag.length();
        int p2 = s.indexOf(eTag, fromIndex);
        if (p2 < 0)
            return null;
        String value = s.substring(fromIndex, p2);
        return value;
    }

    protected void parse(String xml) {
        try {
            if (newParseMethod) {
                parseNew(xml);
            } else {
                parseOld(xml);
            }
        } catch (Exception e) {
            Utils.writeLogMessage("" + Utils.getStackTrace(e), true);
            setParsingError(e);
        }
    }
}
