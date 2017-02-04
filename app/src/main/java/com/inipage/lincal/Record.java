package com.inipage.lincal;

import java.util.Date;

/**
 * Created by Maclyn on 2/4/2017.
 */

public class Record {
    long id;
    String note;
    int totalTime;
    Date startTime;
    Date endTime;
    int billed;
    long taskId;
    long taskColor;
    String taskName;
    String taskCustomerId;

    public Record(long id, String note, int totalTime, Date startTime, Date endTime, int billed, long taskId, long taskColor, String taskName, String taskCustomerId) {
        this.id = id;
        this.note = note;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.billed = billed;
        this.taskId = taskId;
        this.taskColor = taskColor;
        this.taskName = taskName;
        this.taskCustomerId = taskCustomerId;
    }

    public long getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getBilled() {
        return billed;
    }

    public long getTaskId() {
        return taskId;
    }

    public long getTaskColor() {
        return taskColor;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskCustomerId() {
        return taskCustomerId;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
