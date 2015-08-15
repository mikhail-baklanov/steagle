package ru.steagle.protocol.responce;

import ru.steagle.datamodel.DeviceState;

/**
 * Created by bmw on 15.02.14.
 */
public class DeviceStates extends SimpleDictionary<DeviceState> {

    public DeviceStates(String xml) {
        super(xml);
    }

    @Override
    public DeviceState getNewInstance() {
        return new DeviceState();
    }

    @Override
    public String getIdName() {
        return "id_dev_state";
    }

}
