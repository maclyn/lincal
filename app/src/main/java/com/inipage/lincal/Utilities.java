package com.inipage.lincal;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

/**
 * Created by Maclyn on 2/9/2017.
 * TODO: Add a JavaDoc for this class.
 */
public class Utilities {
    public static Date getTodayAtMidnight() {
        Calendar todayAtMidnight = new GregorianCalendar();
        todayAtMidnight.set(Calendar.HOUR_OF_DAY, 23);
        todayAtMidnight.set(Calendar.MINUTE, 59);
        todayAtMidnight.set(Calendar.SECOND, 59);
        todayAtMidnight.set(Calendar.MILLISECOND, 999);
        return todayAtMidnight.getTime();
    }

    public interface MenuPrepListener {
        void onPrepMenu(Menu menu);
    }

    /** See http://stackoverflow.com/questions/15454995/popupmenu-with-icons for a discussion. **/
    public static void attachIconPopupMenu(final View anchor,
                                         final int menuId,
                                         final @Nullable MenuPrepListener prepListener,
                                         final PopupMenu.OnMenuItemClickListener listener){
        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
                menu.inflate(menuId);
                menu.setOnMenuItemClickListener(listener);
                if(prepListener != null) prepListener.onPrepMenu(menu.getMenu());
                MenuPopupHelper helper = new MenuPopupHelper(anchor.getContext(),
                        (MenuBuilder) menu.getMenu(),
                        anchor);
                helper.setForceShowIcon(true);
                helper.show();
            }
        });
    }

    public interface ConfirmationListener {
        void onConfirmed(Context ctx);
        void onRejected(Context ctx);
    }

    public static void showConfirmationDialog(final Context ctx, int titleRes, int messageRes,
                                              int yesRes, int noRes, final ConfirmationListener listener){
        new AlertDialog.Builder(ctx)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setPositiveButton(yesRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onConfirmed(ctx);
                    }
                })
                .setNegativeButton(noRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onRejected(ctx);
                    }
                })
                .show();
    }



    private static final SimpleDateFormat HOURS_MINUTES_SECONDS = new SimpleDateFormat("H:mm:ss", Locale.US);
    private static final SimpleDateFormat MINUTES_SECONDS = new SimpleDateFormat("m:ss", Locale.US);

    /**
     * Returns a duration formatted as h:mm:ss or m:ss. All input paramters don't need adjusting --
     * i.e. putting in "4", "120", "3222", "100000"  will just find out the total duration represented
     * by the sum of all times.
     * @param hours Hours to include.
     * @param minutes Minutes to include.
     * @param seconds Seconds to include.
     * @param millis Milliseconds to include.
     * @return The duration, formatted.
     */
    public static String formatDuration(int hours, int minutes, int seconds, int millis){
        Date tmpDate = new Date();
        long durationMillis = (hours * 60 * 60 * 1000) + (minutes * 60 * 1000) + (seconds * 1000) + millis;

        int totalHours = (int) Math.floor(durationMillis / 60 / 60 / 1000);
        int totalMinutes = (int) Math.floor((durationMillis - (totalHours * 60 * 60 * 1000)) / 60 / 1000);
        int totalSeconds = (int) Math.floor((durationMillis - (totalHours * 60 * 60 * 1000) - (totalMinutes * 60 * 1000)) / 1000);
        //Milliseconds are... whatever.

        tmpDate.setHours(totalHours);
        tmpDate.setMinutes(totalMinutes);
        tmpDate.setSeconds(totalSeconds);
        if(totalHours > 0){
            return HOURS_MINUTES_SECONDS.format(tmpDate);
        } else {
            return MINUTES_SECONDS.format(tmpDate);
        }
    }

    public static void debugNotification(String title, String message, Context ctx){
        /*
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(new Random().nextInt(), new NotificationCompat.Builder(ctx)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_list_black_24dp)
                .setContentIntent(PendingIntent.getActivity(ctx, 5005, new Intent(ctx, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT))
                .build());
         */
    }
}
