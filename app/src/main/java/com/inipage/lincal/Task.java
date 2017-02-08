package com.inipage.lincal;

import android.database.Cursor;

import java.util.Date;
import java.util.Locale;

public class Task {
    private long id;
    private String name;
    private String icon;
    private int color;
    private String reminderDow;
    private String reminderTime;
    private int reminderThreshold;

    //Transient fields; populated as need on-the-fly
    private Date reminderDate;

    public Task(long id, String name, String icon, int color, String reminderDow, String reminderTime, int reminderThreshold) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.color = color;
        this.reminderDow = reminderDow;
        this.reminderTime = reminderTime;
        this.reminderThreshold = reminderThreshold;
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

    private int[] calDows = null;
    public int[] getReminderDowAsCalDow(){
        if(calDows != null) return calDows;

        String[] dows = reminderDow.split(" ");
        calDows = new int[dows.length];
        for(int i = 0; i < dows.length; i++){
            for(int j = 0; j < DatabaseHelper.DB_DOW_ENTRIES.length; j++){
                if(dows[i].toLowerCase(Locale.US).equals(DatabaseHelper.DB_DOW_ENTRIES[j]))
                    calDows[i] = DatabaseHelper.DB_DOW_CAL_FIELDS[j];
            }
        }

        return calDows;
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
}
