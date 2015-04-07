package ru.steagle.datamodel;

import java.util.Date;

/**
 * Created by bmw on 17.02.14.
 */
public class Sensor {
    private Date date;
    private String id;
    private String devId;
    private String typeId;
    private String statusId;
    private String lowBattery;
    private String description;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getLowBattery() {
        return lowBattery;
    }

    public void setLowBattery(String lowBattery) {
        this.lowBattery = lowBattery;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
