package com.inipage.lincal;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class AddTaskActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView iconPicker = (RecyclerView) findViewById(R.id.new_task_icon_picker);
        RecyclerView colorPicker = (RecyclerView) findViewById(R.id.new_task_color_picker);
        iconPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        colorPicker.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        iconPicker.setAdapter(new IVPickerAdapter(new String[] { "ic_add_black_24dp", "ic_check_circle_black_24dp", "ic_history_black_24dp" }));
        colorPicker.setAdapter(new IVPickerAdapter(new int[] {Color.BLACK, Color.BLUE, Color.RED, Color.GREEN}));
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
                //TODO: Save
                finish();
                return true;
        }

        return false;
    }
}
