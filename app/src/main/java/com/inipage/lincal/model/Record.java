package com.inipage.lincal.model;

import java.util.Date;

public class Record {
    long id;
    String note;
    int totalTime;
    Date startTime;
    Date endTime;
    long taskId;
    long taskColor;
    String taskName;
    int todoId;

    public Record(long id, String note, int totalTime, Date startTime, Date endTime, long taskId,
                  long taskColor, String taskName, int todoId) {
        this.id = id;
        this.note = note;
        this.totalTime = totalTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.taskId = taskId;
        this.taskColor = taskColor;
        this.taskName = taskName;
        this.todoId = todoId;
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

    public int getTodoId() {
        return todoId;
    }

    public boolean hasTodo(){
        return todoId != -1;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
