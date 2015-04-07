package ru.steagle.datamodel;

/**
 * Created by bmw on 15.02.14.
 */
public class Tarif {
    private String id;
    private String description;
    private String sum;

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

    public String getSum() {
        return sum;
    }

    public void setSum(String sum) {
        this.sum = sum;
    }
}
