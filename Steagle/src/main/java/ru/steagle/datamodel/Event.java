package ru.steagle.datamodel;

import java.util.Date;

/**
 * Created by bmw on 17.02.14.
 */
public class Event {
    private Date date;
    private String levelId;
    private String eventTypeId;
    private String sensorId;
    private String devModeId;
    private String devLastModeId;
    private String devId;
    private String devModeSrcId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getLevelId() {
        return levelId;
    }

    public void setLevelId(String levelId) {
        this.levelId = levelId;
    }

    public String getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(String eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getDevModeId() {
        return devModeId;
    }

    public void setDevModeId(String devModeId) {
        this.devModeId = devModeId;
    }

    public String getDevLastModeId() {
        return devLastModeId;
    }

    public void setDevLastModeId(String devLastModeId) {
        this.devLastModeId = devLastModeId;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getDevModeSrcId() {
        return devModeSrcId;
    }

    public void setDevModeSrcId(String devModeSrcId) {
        this.devModeSrcId = devModeSrcId;
    }
}
