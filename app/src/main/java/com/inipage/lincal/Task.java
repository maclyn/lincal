package com.inipage.lincal;

import android.database.Cursor;

import java.util.Date;

public class Task {
    private long id;
    private String name;
    private String icon;
    private int color;
    private String reminderDow;
    private String reminderTime;
    private int reminderThreshold;
    private String customerId;

    //Transient fields; populated as need on-the-fly
    private Date reminderDate;

    public Task(long id, String name, String icon, int color, String reminderDow, String reminderTime, int reminderThreshold, String customerId) {
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

    public boolean hasReminder(){
        return reminderThreshold != 0;
    }

    public Date getReminderTimeAsDate(){
        try {
            return reminderDate == null ?
                    (reminderDate = DatabaseHelper.DB_REMINDER_TIME_FORMAT.parse(reminderTime)) :
                    reminderDate;
        } catch (Exception e){
            return null;
        }
    }

    public int getReminderThreshold() {
        return reminderThreshold;
    }

    public String getCustomerId() {
        return customerId;
    }
}
