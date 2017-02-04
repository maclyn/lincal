package com.inipage.lincal;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AddTaskActivity extends AppCompatActivity {
    int hourOfDay = 12;
    int minuteOfDay = 0;

    CheckBox enableReminder;
    EditText name;
    RecyclerView iconPicker;
    IVPickerAdapter iconAdapter;
    RecyclerView colorPicker;
    IVPickerAdapter colorAdapter;
    Button reminderTime;
    Button reminderDow;
    NumberPicker countPicker;

    ///Expediency is the mortal enemy of accuracy
    boolean sunday = false;
    boolean monday = true;
    boolean tuesday = true;
    boolean wednesday = true;
    boolean thursday = true;
    boolean friday = true;
    boolean saturday = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = (EditText) findViewById(R.id.new_task_name);
        iconPicker = (RecyclerView) findViewById(R.id.new_task_icon_picker);
        colorPicker = (RecyclerView) findViewById(R.id.new_task_color_picker);
        reminderTime = (Button) findViewById(R.id.reminder_time);
        reminderDow = (Button) findViewById(R.id.reminder_dow);
        countPicker = (NumberPicker) findViewById(R.id.count_picker);
        enableReminder = (CheckBox) findViewById(R.id.enable_reminder);

        iconPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        colorPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        iconAdapter = new IVPickerAdapter(Constants.TASK_ICONS);
        iconPicker.setAdapter(iconAdapter);
        colorAdapter = new IVPickerAdapter(Constants.TASK_COLORS);
        colorPicker.setAdapter(colorAdapter);
        reminderTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(view.getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                        hourOfDay = hour;
                        minuteOfDay = minute;

                        Date date = new Date();
                        date.setHours(hourOfDay);
                        date.setMinutes(minute);
                        reminderTime.setText(new SimpleDateFormat("h:mm aa").format(date));
                    }
                }, hourOfDay, minuteOfDay, false).show();
            }
        });
        reminderDow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View customView = LayoutInflater.from(AddTaskActivity.this).inflate(R.layout.dialog_dow, null);

                final CheckBox mondayView = (CheckBox) customView.findViewById(R.id.monday);
                final CheckBox tuesdayView = (CheckBox) customView.findViewById(R.id.tuesday);
                final CheckBox wednesdayView = (CheckBox) customView.findViewById(R.id.wednesday);
                final CheckBox thursdayView = (CheckBox) customView.findViewById(R.id.thursday);
                final CheckBox fridayView = (CheckBox) customView.findViewById(R.id.friday);
                final CheckBox saturdayView = (CheckBox) customView.findViewById(R.id.saturday);
                final CheckBox sundayView = (CheckBox) customView.findViewById(R.id.sunday);

                mondayView.setChecked(monday);
                tuesdayView.setChecked(tuesday);
                wednesdayView.setChecked(wednesday);
                thursdayView.setChecked(thursday);
                fridayView.setChecked(friday);
                saturdayView.setChecked(saturday);
                sundayView.setChecked(sunday);

                new AlertDialog.Builder(AddTaskActivity.this)
                        .setTitle("Pick Reminder Days of Week")
                        .setView(customView)
                        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                monday = mondayView.isChecked();
                                tuesday = tuesdayView.isChecked();
                                wednesday = wednesdayView.isChecked();
                                thursday = thursdayView.isChecked();
                                friday = fridayView.isChecked();
                                saturday = saturdayView.isChecked();
                                sunday = sundayView.isChecked();

                                String buttonString = "";
                                if(sunday) buttonString += "Su ";
                                if(monday) buttonString += "Mo ";
                                if(tuesday) buttonString += "Tu ";
                                if(wednesday) buttonString += "We ";
                                if(thursday) buttonString += "Th ";
                                if(friday) buttonString += "Fr ";
                                if(saturday) buttonString += "Sa";

                                reminderDow.setText(buttonString);
                            }
                        })
                        .show();
            }
        });
        countPicker.setMinValue(1);
        countPicker.setMaxValue(360);
        enableReminder.setChecked(true);
        enableReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                reminderTime.setEnabled(b);
                reminderDow.setEnabled(b);
                countPicker.setEnabled(b);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_save:
                //TODO: Check for duplicate reminder times here
                if(DatabaseEditor.getInstance(this).addNewTask(
                        name.getText().toString(),
                        iconAdapter.getSelectedResource(),
                        colorAdapter.getSelectedColor(),
                        reminderTime.getText().toString(),
                        reminderDow.getText().toString(),
                        enableReminder.isChecked() ? countPicker.getValue() : 0)) {
                    Toast.makeText(this, "Task saved.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "You can't have duplicate task names.", Toast.LENGTH_SHORT).show();
                }
                return true;
        }

        return false;
    }
}
