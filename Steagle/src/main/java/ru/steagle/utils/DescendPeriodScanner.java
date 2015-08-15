package ru.steagle.utils;

import java.util.Date;

/**
 * Created by bmw on 22.03.14.
 */
public class DescendPeriodScanner {
    private static final Date minDate = new Date(1, 2, 3);
    private static final Date maxDate = new Date(2345, 6, 7);
    private Date startDate;
    private Date endDate;
    private Date currentDate;

    public void setPeriod(Date startD, Date endD) {
        startDate = startD == null ? minDate : Utils.getDay(startD);
        endDate = endD == null ? maxDate : Utils.getNextDay(endD);
        currentDate = endDate;
    }

    public void resetPointer() {
        currentDate = endDate;
    }

    public void setPointer(Date date) {
        currentDate = date;
    }

    public void finish() {
        currentDate = startDate;
    }

    public Date getUnscannedStartDate() {
        return startDate;
    }

    public Date getUnscannedEndDate() {
        return currentDate;
    }

    public boolean isUnscannedEmpty() {
        return currentDate.compareTo(startDate) <= 0;
    }
}
