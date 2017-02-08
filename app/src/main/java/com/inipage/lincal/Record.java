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
    long taskId;
    long taskColor;
    String taskName;

    public Record(long id, String note, int totalTime, Date startTime, Date endTime, long taskId, long taskColor, String taskName) {
        this.id = id;
        this.note = note;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskId = taskId;
        this.taskColor = taskColor;
        this.taskName = taskName;
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

    public long getTaskId() {
        return taskId;
    }

    public long getTaskColor() {
        return taskColor;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
