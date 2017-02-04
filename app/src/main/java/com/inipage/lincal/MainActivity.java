package com.inipage.lincal;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView mNavView;
    FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavView = (BottomNavigationView) findViewById(R.id.bottom_tabs);
        mContainer = (FrameLayout) findViewById(R.id.fragment_container);

        DatabaseHelper helper = new DatabaseHelper(this, DatabaseHelper.FILENAME, null, DatabaseHelper.VERSION);
        helper.getWritableDatabase();
    }
}
