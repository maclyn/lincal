package com.inipage.lincal;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ReminderService extends Service {
    public ReminderService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO: Finish this up, eventually?
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
