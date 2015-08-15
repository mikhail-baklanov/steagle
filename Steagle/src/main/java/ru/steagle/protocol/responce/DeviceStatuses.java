package ru.steagle.protocol.responce;

import ru.steagle.datamodel.DeviceStatus;

/**
 * Created by bmw on 15.02.14.
 */
public class DeviceStatuses extends SimpleDictionary<DeviceStatus> {

    public DeviceStatuses(String xml) {
        super(xml);
    }

    @Override
    public DeviceStatus getNewInstance() {
        return new DeviceStatus();
    }

    @Override
    public String getIdName() {
        return "id_status_dev";
    }

}
