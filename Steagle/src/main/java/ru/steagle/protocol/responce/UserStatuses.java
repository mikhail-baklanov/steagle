package ru.steagle.protocol.responce;

import ru.steagle.datamodel.SimpleDictionary;
import ru.steagle.datamodel.UserStatus;

/**
 * Created by bmw on 15.02.14.
 */
public class UserStatuses extends SimpleDictionary<UserStatus> {

    public UserStatuses(String xml) {
        super(xml);
    }

    @Override
    public UserStatus getNewInstance() {
        return new UserStatus();
    }

    @Override
    public String getIdName() {
        return "id_status";
    }

}
