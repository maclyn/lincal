package com.inipage.lincal;

public class TaskToday extends Task {
    private int secondsSoFar;

    public TaskToday(Task t, int secondsSoFar) {
        super(t.getId(), t.getName(), t.getIcon(), t.getColor(), t.getReminderDow(), t.getReminderTime(), t.getReminderThreshold(), t.getCustomerId());
        this.secondsSoFar = secondsSoFar;
    }

    public int getSecondsSoFar() {
        return secondsSoFar;
    }
}
