package ru.steagle.datamodel;

import java.util.Date;

/**
 * Created by bmw on 17.02.14.
 */
public class Notification {
    private Date date;
    private String text;
    private String deviceId;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
