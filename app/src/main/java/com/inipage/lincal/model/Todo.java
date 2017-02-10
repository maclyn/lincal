package com.inipage.lincal.model;

import com.inipage.lincal.db.DatabaseHelper;

import java.util.Date;

/**
 * The model of a to-do item.
 */
public class Todo {
    private long id;
    private String title;
    private String dueDate;
    private boolean complete;
    private long taskId;
    private int importance;

    //Transient Date field; expanded when needed
    private Date dueDateAsDate;

    public Todo(long id, String title, String dueDate, boolean complete, long taskId, int importance) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.complete = complete;
        this.taskId = taskId;
        this.importance = importance;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDueDate() {
        return dueDate;
    }

    public Date getDueDateAsDate(){
        if(dueDateAsDate != null) return dueDateAsDate;

        try {
            dueDateAsDate = DatabaseHelper.DB_RECORD_DATE_FORMAT.parse(dueDate);
        } catch (Exception uhOh) {}

        return dueDateAsDate;
    }

    public boolean isComplete() {
        return complete;
    }

    public long getTaskId() {
        return taskId;
    }

    public int getImportance() {
        return importance;
    }

    public void setCompleted(boolean completed) {
        this.complete = completed;
    }
}
