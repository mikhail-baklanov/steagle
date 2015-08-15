package ru.steagle.protocol.responce;

import ru.steagle.datamodel.DeviceMode;

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
