package com.inipage.lincal.model;

public class TaskToday extends Task {
    private int secondsSoFar;

    public TaskToday(Task t, int secondsSoFar) {
        super(t.getId(), t.getName(), t.getIcon(), t.getColor(), t.getReminderDow(),
                t.getReminderTime(), t.getReminderThreshold(), t.getProductivityLevel(),
                t.isArchived());
        this.secondsSoFar = secondsSoFar;
    }

    public int getSecondsSoFar() {
        return secondsSoFar;
    }

    public void setSecondsSoFar(int secondsSoFar) {
        this.secondsSoFar = secondsSoFar;
    }
}
