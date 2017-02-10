package com.inipage.lincal.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent scheduleReminders = new Intent(context, ReminderService.class);
        scheduleReminders.setAction(ReminderService.ACTION_UPDATE_REMINDERS);
        context.startService(scheduleReminders);
    }
}
