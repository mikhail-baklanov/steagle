package ru.steagle.protocol.responce;

import ru.steagle.datamodel.SensorStatus;

/**
 * Created by bmw on 15.02.14.
 */
public class SensorStatuses extends SimpleDictionary<SensorStatus> {

    public SensorStatuses(String xml) {
        super(xml);
    }

    @Override
    public SensorStatus getNewInstance() {
        return new SensorStatus();
    }

    @Override
    public String getIdName() {
        return "id_status_sensor";
    }

}
