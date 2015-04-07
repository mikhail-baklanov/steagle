package ru.steagle.datamodel;

import java.util.Date;

/**
 * Created by bmw on 17.02.14.
 */
public class Pay {
    private Date date;
    private String description;
    private String sum;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
