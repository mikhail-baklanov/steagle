package ru.steagle.protocol.responce;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

import ru.steagle.datamodel.DeviceMode;
import ru.steagle.datamodel.DeviceStatus;
import ru.steagle.datamodel.SimpleDictionary;

/**
 * Created by bmw on 15.02.14.
 */
public class DevModes  extends SimpleDictionary<DeviceMode> {

    public DevModes(String xml) {
        super(xml);
    }

    @Override
    public DeviceMode getNewInstance() {
        return new DeviceMode();
    }

    @Override
    public String getIdName() {
        return "id_dev_mode";
    }
    @Override
    public String getDescriptionName() {
        return "descr_dev_mode";
    }

}
