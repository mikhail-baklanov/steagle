package ru.steagle.datamodel;

/**
 * Created by bmw on 15.02.14.
 */
public class SimpleDictionaryElem {
    private String id;
    private String description;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
