package com.inipage.lincal;

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.inipage.lincal.background.OopsReceiver;

/**
 * Created by Maclyn on 2/16/2017.
 * TODO: Add a JavaDoc for this class.
 */

public class AppClass extends Application implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "AppClass";

    private static final int OOPSIE_DAISY_NID = 1501;
    private static final int SEND_EMAIL_PENDING_INTENT_ID = 1501;

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Log.e(TAG, "Uh-oh", e);

        //Craaaaaap. Offer to view and email from a notification.
        String[] stackTrace = new String[e.getStackTrace().length];
        for(int i = 0; i < e.getStackTrace().length; i++){
            StackTraceElement ste = e.getStackTrace()[i];
            stackTrace[i] = ste.toString();
        }
        String message = e.getMessage() == null ? "(no message)" : e.getMessage();

        Intent broadcast = new Intent(OopsReceiver.ACTION_SEND_BUG_REPORT_EMAIL);
        broadcast.putExtra(OopsReceiver.EXTRA_STACKTRACE_STR_ARR, stackTrace);
        broadcast.putExtra(OopsReceiver.EXTRA_EXCEPTION_MESSAGE_STR, message);
        PendingIntent bPi = PendingIntent.getBroadcast(this, SEND_EMAIL_PENDING_INTENT_ID, broadcast, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(OOPSIE_DAISY_NID, new NotificationCompat.Builder(this)
            .setContentTitle(getString(R.string.app_name) + " has crashed: " + message)
            .setContentText(stackTrace.length > 0 ? stackTrace[0].toString() : "(no stack trace")
            .setContentIntent(bPi)
            .setSmallIcon(R.drawable.ic_error_outline_black_24dp)
            .build());

        System.exit(1);
    }
}
