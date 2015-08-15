package ru.steagle.protocol.responce;

import ru.steagle.datamodel.SensorType;

/**
 * Created by bmw on 15.02.14.
 */
public class SensorTypes extends SimpleDictionary<SensorType> {

    public SensorTypes(String xml) {
        super(xml);
    }

    @Override
    public SensorType getNewInstance() {
        return new SensorType();
    }

    @Override
    public String getIdName() {
        return "id_type_sensor";
    }

    @Override
    public String getDescriptionName() {
        return "descr_sensors_type";
    }
}
