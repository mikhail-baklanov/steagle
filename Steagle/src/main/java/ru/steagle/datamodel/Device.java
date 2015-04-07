package ru.steagle.datamodel;

import java.util.Date;

/**
 * Created by bmw on 17.02.14.
 */
public class Device extends SimpleDictionaryElem {
    private Date date;
    private String typeId;
    private String lastModeId;
    private Date dateLastModeId;
    private String statusId;
    private String stateId;
    private int sensorOnCounter;

    private int sensorOffCounter;

    public int getSensorOffCounter() {
        return sensorOffCounter;
    }

    public void setSensorOffCounter(int sensorOffCounter) {
        this.sensorOffCounter = sensorOffCounter;
    }

    public int getSensorOnCounter() {
        return sensorOnCounter;
    }

    public void setSensorOnCounter(int sensorOnCounter) {
        this.sensorOnCounter = sensorOnCounter;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public String getLastModeId() {
        return lastModeId;
    }

    public void setLastModeId(String lastModeId) {
        this.lastModeId = lastModeId;
    }

    public Date getDateLastModeId() {
        return dateLastModeId;
    }

    public void setDateLastModeId(Date dateLastModeId) {
        this.dateLastModeId = dateLastModeId;
    }

    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

}
