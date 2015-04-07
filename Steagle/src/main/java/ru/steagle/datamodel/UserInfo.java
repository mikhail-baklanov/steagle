package ru.steagle.datamodel;

import org.xml.sax.Attributes;

import android.sax.Element;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.util.Xml;

import java.util.StringTokenizer;

import ru.steagle.protocol.request.ChangeNotifyFlagCommand;
import ru.steagle.protocol.responce.BaseResult;

public class UserInfo extends BaseResult {
    public static final String NOTIFY_PHONES_ATTRIB = "avphone";
    public static final String NOTIFY_EMAILS_ATTRIB = "aemail";
    public static final String NOTIFY_SMS_ATTRIB = "sms";
    private String statusId;
    private String notifyPhones;
    private String notifySms;
    private String notifyEmails;
    private String userName;
    private String balance;
    private String currencyId;
    private String timeZoneId;
    private String tarifId;
    private String account;
    private String email;

    private String phone;

    private boolean phoneNotifyEnabled;
    private boolean smsNotifyEnabled;
    private boolean emailNotifyEnabled;


    public UserInfo(String xml) {
        parse(xml);
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getTarifId() {
        return tarifId;
    }

    public String getCurrencyId() {
        return currencyId;
    }

    public String getAccount() {
        return account;
    }

    public boolean isPhoneNotifyEnabled() {
        return phoneNotifyEnabled;
    }

    public boolean isSmsNotifyEnabled() {
        return smsNotifyEnabled;
    }

    public boolean isEmailNotifyEnabled() {
        return emailNotifyEnabled;
    }

    public String getUserName() {
        return userName;
    }

    public String getBalance() {
        return balance;
    }

    public String getStatusId() {
        return statusId;
    }

    public String getNotifyPhones() {
        return notifyPhones;
    }

    public String getNotifySms() {
        return notifySms;
    }

    public String getNotifyEmails() {
        return notifyEmails;
    }

    protected void parse(String xml) {
        RootElement root = new RootElement("root");
        Element tag = root.getChild("user");
        tag.setStartElementListener(new StartElementListener() {
            public void start(Attributes attrib) {
                readBaseFields(attrib);
                statusId = attrib.getValue("id_status");

                notifyPhones = attrib.getValue(NOTIFY_PHONES_ATTRIB);
                notifySms = attrib.getValue(NOTIFY_SMS_ATTRIB);
                notifyEmails = attrib.getValue(NOTIFY_EMAILS_ATTRIB);
                userName = attrib.getValue("name");
                balance = attrib.getValue("balance");
                account = attrib.getValue("account");
                currencyId = attrib.getValue("id_currency");
                timeZoneId = attrib.getValue("tz");
                tarifId = attrib.getValue("id_tarif");
                phone = attrib.getValue("phone");
                email = attrib.getValue("mail");
                String notifyFlags = attrib.getValue("flags_users");
                parseNotifyFlags(notifyFlags);
            }
        });
        try {
            Xml.parse(xml, root.getContentHandler());
        } catch (Exception e) {
            setParsingError(e);
        }
    }

    private void parseNotifyFlags(String notifyFlags) {
        phoneNotifyEnabled = true;
        smsNotifyEnabled = true;
        emailNotifyEnabled = true;
        if (notifyFlags != null) {
            StringTokenizer st = new StringTokenizer(notifyFlags, ",");
            while (st.hasMoreTokens()) {
                String flag = st.nextToken();
                switch (flag) {
                    case ChangeNotifyFlagCommand.SMS:
                        smsNotifyEnabled = false;
                        break;
                    case ChangeNotifyFlagCommand.EMAIL:
                        emailNotifyEnabled = false;
                        break;
                    case ChangeNotifyFlagCommand.PHONE:
                        phoneNotifyEnabled = false;
                        break;
                }
            }
        }
    }


    public String getTimeZoneId() {
        return timeZoneId;
    }
}

