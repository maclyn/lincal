package com.inipage.lincal;

import android.database.Cursor;

/**
 * Created by Maclyn on 2/4/2017.
 */

public class Task {
    private long id;
    private String name;
    private String icon;
    private int color;
    private String reminderDow;
    private String reminderTime;
    private int reminderThreshold;
    private long customerId;

    public Task(long id, String name, String icon, int color, String reminderDow, String reminderTime, int reminderThreshold, long customerId) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.reminderDow = reminderDow;
        this.reminderTime = reminderTime;
        this.reminderThreshold = reminderThreshold;
        this.customerId = customerId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }

    public String getReminderDow() {
        return reminderDow;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public int getReminderThreshold() {
        return reminderThreshold;
    }

    public long getCustomerId() {
        return customerId;
    }
}
