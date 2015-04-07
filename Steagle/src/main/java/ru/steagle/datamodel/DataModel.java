package ru.steagle.datamodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bmw on 10.02.14.
 */
public class DataModel {
    private List<String> notificationPhoneList = new ArrayList<>();
    private List<String> notificationSmsList = new ArrayList<>();
    private List<String> notificationEmailList = new ArrayList<>();
    private List<TimeZone> timeZones = new ArrayList<>();
    private List<Device> devices = new ArrayList<>();
    private List<Sensor> sensors = new ArrayList<>();
    private Map<String, UserStatus> userStatusesMap = new HashMap<>();
    private Map<String, Currency> currenciesMap = new HashMap<>();
    private Map<String, Level> levelsMap = new HashMap<>();
    private Map<String, TimeZone> timeZonesMap = new HashMap<>();
    private Map<String, DevModeSrc> deviceModeSourcesMap = new HashMap<>();
    private Map<String, DeviceMode> deviceModesMap = new HashMap<>();
    private Map<String, DeviceMode> deviceModesByKeyMap = new HashMap<>();
    private Map<String, Device> devicesMap = new HashMap<>();
    private Map<String, DeviceStatus> deviceStatusesMap = new HashMap<>();
    private Map<String, DeviceStatus> deviceStatusesByKeyMap = new HashMap<>();
    private Map<String, DeviceState> deviceStatesMap = new HashMap<>();
    private Map<String, DeviceState> deviceStatesByKeyMap = new HashMap<>();
    private Map<String, SensorStatus> sensorStatusesMap = new HashMap<>();
    private Map<String, SensorStatus> sensorStatusesByKeyMap = new HashMap<>();
    private Map<String, Sensor> sensorsMap = new HashMap<>();
    private Map<String, Tarif> tarifsMap = new HashMap<>();

    private String userName;
    private String email;
    private String phone;
    private String balance;
    private String userStatusId;
    private String currencyId;
    private String tarifId;
    private String timeZoneId;
    private String account;
    private boolean phoneNotifyEnabled;
    private boolean smsNotifyEnabled;

    public List<Device> getDevices() {
        return devices;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private boolean emailNotifyEnabled;

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setTarifId(String tarifId) {
        this.tarifId = tarifId;
    }

    public void setTimeZoneId(String timeZoneId) {
        this.timeZoneId = timeZoneId;
    }

    public void setCurrencyId(String currencyId) {
        this.currencyId = currencyId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setUserStatusId(String userStatusId) {
        this.userStatusId = userStatusId;
    }

    public boolean isPhoneNotifyEnabled() {
        return phoneNotifyEnabled;
    }

    public void setPhoneNotifyEnabled(boolean phoneNotifyEnabled) {
        this.phoneNotifyEnabled = phoneNotifyEnabled;
    }

    public boolean isSmsNotifyEnabled() {
        return smsNotifyEnabled;
    }

    public void setSmsNotifyEnabled(boolean smsNotifyEnabled) {
        this.smsNotifyEnabled = smsNotifyEnabled;
    }

    public boolean isEmailNotifyEnabled() {
        return emailNotifyEnabled;
    }

    public void setEmailNotifyEnabled(boolean emailNotifyEnabled) {
        this.emailNotifyEnabled = emailNotifyEnabled;
    }

    public void setUserInfo(UserInfo userInfo) {
        setNotificationPhoneList(userInfo.getNotifyPhones());
        setNotificationSmsList(userInfo.getNotifySms());
        setNotificationEmailList(userInfo.getNotifyEmails());
        setUserName(userInfo.getUserName());
        setBalance(userInfo.getBalance());
        setPhoneNotifyEnabled(userInfo.isPhoneNotifyEnabled());
        setSmsNotifyEnabled(userInfo.isSmsNotifyEnabled());
        setEmailNotifyEnabled(userInfo.isEmailNotifyEnabled());
        setUserStatusId(userInfo.getStatusId());
        setAccount(userInfo.getAccount());
        setCurrencyId(userInfo.getCurrencyId());
        setTimeZoneId(userInfo.getTimeZoneId());
        setTarifId(userInfo.getTarifId());
        setPhone(userInfo.getPhone());
        setEmail(userInfo.getEmail());
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private List<String> splite(String delimitedValues) {
        List<String> result = new ArrayList<>();
        if (delimitedValues != null) {
            String[] list = delimitedValues.split(",");
            if (list != null) {
                result.addAll(Arrays.asList(list));
            }
        }
        return result;
    }

    public void setNotificationPhoneList(String notificationPhoneList) {
        this.notificationPhoneList = splite(notificationPhoneList);
    }

    public List<String> getNotificationSmsList() {
        return notificationSmsList;
    }

    public void setNotificationSmsList(String notificationSmsList) {
        this.notificationSmsList = splite(notificationSmsList);
    }

    public List<String> getNotificationEmailList() {
        return notificationEmailList;
    }

    public void setNotificationEmailList(String notificationEmailList) {
        this.notificationEmailList = splite(notificationEmailList);
    }

    public List<String> getNotificationPhoneList() {
        return notificationPhoneList;
    }

    public void setUserStatuses(List<UserStatus> list) {
        userStatusesMap.clear();
        for (UserStatus elem : list) {
            userStatusesMap.put(elem.getId(), elem);
        }
    }

    public String getUserStatus() {
        UserStatus elem = userStatusesMap.get(userStatusId);
        if (elem == null)
            return null;
        else
            return elem.getDescription();
    }

    public List<TimeZone> getTimeZones() {
        return timeZones;
    }

    public TimeZone getTimeZone() {
        return timeZonesMap.get(timeZoneId);
    }

    public void setCurrencies(List<Currency> list) {
        currenciesMap.clear();
        for (Currency elem : list) {
            currenciesMap.put(elem.getId(), elem);
        }
    }

    public void setLevels(List<Level> list) {
        levelsMap.clear();
        for (Level elem : list) {
            levelsMap.put(elem.getKey(), elem);
        }
    }

    public String getImportantLevelId() {
        Level level = levelsMap.get("alert");
        return level == null ? null : level.getId();
    }

    public String getAllLevelId() {
        Level level = levelsMap.get("all");
        return level == null ? null : level.getId();
    }

    public String getCurrency() {
        Currency elem = currenciesMap.get(currencyId);
        if (elem == null)
            return null;
        else
            return elem.getDescription();
    }

    public void setTimeZones(List<TimeZone> list) {
        timeZones = list;
        timeZonesMap.clear();
        for (TimeZone elem : list) {
            timeZonesMap.put(elem.getName(), elem);
        }
    }

    public void setDevModeSrcs(List<DevModeSrc> list) {
        deviceModeSourcesMap.clear();
        for (DevModeSrc elem : list) {
            deviceModeSourcesMap.put(elem.getId(), elem);
        }
    }

    public void setDevModes(List<DeviceMode> list) {
        deviceModesMap.clear();
        deviceModesByKeyMap.clear();
        for (DeviceMode elem : list) {
            deviceModesMap.put(elem.getId(), elem);
            deviceModesByKeyMap.put(elem.getKey(), elem);
        }
    }

    public void setDevices(List<Device> list) {
        devices = list;
        devicesMap.clear();
        for (Device elem : list) {
            devicesMap.put(elem.getId(), elem);
        }
        updateSensorInfoInDevices();
    }

    public void setTarifs(List<Tarif> list) {
        tarifsMap.clear();
        for (Tarif elem : list) {
            tarifsMap.put(elem.getId(), elem);
        }
    }

    public Tarif getTarif() {
        return tarifsMap.get(tarifId);
    }

    public String getDevModeSrcDescription(String devModeSrcId) {
        DevModeSrc d = deviceModeSourcesMap.get(devModeSrcId);
        return d == null ? null : d.getDescription();
    }

    public String getDevModeDescription(String devModeId) {
        DeviceMode d = deviceModesMap.get(devModeId);
        return d == null ? null : d.getDescription();
    }

    public void setSensors(List<Sensor> list) {
        sensors = list;
        sensorsMap.clear();
        for (Sensor elem : list) {
            sensorsMap.put(elem.getId(), elem);
        }
        updateSensorInfoInDevices();
    }

    public void updateSensorInfoInDevices() {
        for (Device device : getDevices()) {
            device.setSensorOnCounter(0);
            device.setSensorOffCounter(0);
        }
        String allowSensorStatusId = getAllowSensorStatusId();
        String suspendSensorStatusId = getSuspendSensorStatusId();
        for (Sensor elem : sensorsMap.values()) {
            Device device = devicesMap.get(elem.getDevId());
            if (device != null) {
                if (allowSensorStatusId != null && allowSensorStatusId.equals(elem.getStatusId())) {
                    device.setSensorOnCounter(device.getSensorOnCounter() + 1);
                } else if (suspendSensorStatusId != null && suspendSensorStatusId.equals(elem.getStatusId())) {
                    device.setSensorOffCounter(device.getSensorOffCounter() + 1);
                }
            }
        }
    }

    public String getSensorDescription(String sensorId) {
        Sensor s = sensorsMap.get(sensorId);
        return s == null ? null : s.getDescription();
    }

    public void setDeviceStatuses(List<DeviceStatus> list) {
        deviceStatusesMap.clear();
        deviceStatusesByKeyMap.clear();
        for (DeviceStatus elem : list) {
            deviceStatusesMap.put(elem.getId(), elem);
            deviceStatusesByKeyMap.put(elem.getKey(), elem);
        }
    }

    public void setSensorStatuses(List<SensorStatus> list) {
        sensorStatusesMap.clear();
        sensorStatusesByKeyMap.clear();
        for (SensorStatus elem : list) {
            sensorStatusesMap.put(elem.getId(), elem);
            sensorStatusesByKeyMap.put(elem.getKey(), elem);
        }
    }

    public DeviceStatus getDeviceStatus(String deviceStatusId) {
        return deviceStatusesMap.get(deviceStatusId);
    }

    public String getAllowDeviceStatusId() {
        DeviceStatus s = deviceStatusesByKeyMap.get("allow");
        return s == null ? null : s.getId();
    }

    public String getSuspendDeviceStatusId() {
        DeviceStatus s = deviceStatusesByKeyMap.get("suspend");
        return s == null ? null : s.getId();
    }

    public String getAllowSensorStatusId() {
        SensorStatus s = sensorStatusesByKeyMap.get("allow");
        return s == null ? null : s.getId();
    }

    public String getSuspendSensorStatusId() {
        SensorStatus s = sensorStatusesByKeyMap.get("suspend");
        return s == null ? null : s.getId();
    }

    public String getAllowDeviceModeId() {
        DeviceMode s = deviceModesByKeyMap.get("NORMAL"); // нормальный
        return s == null ? null : s.getId();
    }

    public String getSuspendDeviceModeId() {
        DeviceMode s = deviceModesByKeyMap.get("ALARM_S1"); // постановка на охрану
        return s == null ? null : s.getId();
    }

    public void setDeviceName(String deviceId, String deviceName) {
        Device device = devicesMap.get(deviceId);
        if (device != null)
            device.setDescription(deviceName);
    }

    public void clearDeviceStatus(String deviceId) {
        Device device = devicesMap.get(deviceId);
        if (device != null) {
            device.setStatusId(null);
        }
    }

    public List<Sensor> getSensorsOfDevice(String deviceId) {
        List<Sensor> list = new ArrayList<>();
        if (deviceId != null)
            for (Sensor sensor : sensors)
                if (deviceId.equals(sensor.getDevId()))
                    list.add(sensor);
        return list;
    }

    public void setDeviceStates(List<DeviceState> list) {
        deviceStatesMap.clear();
        deviceStatesByKeyMap.clear();
        for (DeviceState elem : list) {
            deviceStatesMap.put(elem.getId(), elem);
            deviceStatesByKeyMap.put(elem.getKey(), elem);
        }
    }

    public String getOnlineDeviceStateId() {
        DeviceState s = deviceStatesByKeyMap.get("online"); // в сети
        return s == null ? null : s.getId();
    }

    public String getOfflineDeviceStateId() {
        DeviceState s = deviceStatesByKeyMap.get("offline"); // не в сети
        return s == null ? null : s.getId();
    }
}
