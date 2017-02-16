package com.inipage.lincal.background;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.inipage.lincal.R;

public class OopsReceiver extends BroadcastReceiver {
    public static final String ACTION_SEND_BUG_REPORT_EMAIL = "com.inipage.lincal.send_report";
    public static final String EXTRA_STACKTRACE_STR_ARR = "stacktrace";
    public static final String EXTRA_EXCEPTION_MESSAGE_STR = "msg";

    public OopsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String[] stacktrace = intent.getStringArrayExtra(EXTRA_STACKTRACE_STR_ARR);
        String message = intent.getStringExtra(EXTRA_EXCEPTION_MESSAGE_STR);

        //Subject
        String emailSubject = context.getString(R.string.app_name) + " has crashed";
        String emailMsg = "Here's a description of what happened:\n\n";
        emailMsg += "Crash message: " + message + "\nStack trace: \n";
        for(String el : stacktrace){
            emailMsg += el + "\n";
        }

        Intent sendEmail = new Intent(Intent.ACTION_SEND);
        sendEmail.putExtra(Intent.EXTRA_EMAIL, new String[] { "mgb2163@columbia.edu" });
        sendEmail.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        sendEmail.putExtra(Intent.EXTRA_TEXT, emailMsg);
        sendEmail.setType("*/*");
        sendEmail.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Intent clone = (Intent) sendEmail.clone();
        clone.setData(Uri.parse("mailto:"));
        if(clone.resolveActivity(context.getPackageManager()) != null){ //We have a mail app
            context.startActivity(clone);
        } else { //We don't have one; but we probably have a share handler...?
            try {
                context.startActivity(sendEmail);
            } catch (Exception noMatchingActivity) {
                Toast.makeText(context, R.string.no_share_app, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
