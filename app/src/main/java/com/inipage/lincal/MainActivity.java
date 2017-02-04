package com.inipage.lincal;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    BottomNavigationView mNavView;
    FrameLayout mContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavView = (BottomNavigationView) findViewById(R.id.bottom_tabs);
        mContainer = (FrameLayout) findViewById(R.id.fragment_container);

        //TODO: At some point, figure out how this is *actually* supposed to work w/o taking this out of the SL
        View divider = new View(this);
        divider.setBackgroundColor(
                ContextCompat.getColor(this, android.support.design.R.color.design_bottom_navigation_shadow_color));
        FrameLayout.LayoutParams dividerParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(
                        android.support.design.R.dimen.design_bottom_navigation_shadow_height));
        divider.setLayoutParams(dividerParams);
        mNavView.addView(divider);

        mNavView.setOnNavigationItemSelectedListener(this);
        replaceFragment(R.id.today);
    }

    public void replaceFragment(int itemId){
        Fragment fragmentForReplacing = DummyFragment.newInstance();
        switch(itemId){
            case R.id.today:
                fragmentForReplacing = TodayFragment.newInstance();
                break;
            case R.id.history:
                fragmentForReplacing = HistoryFragment.newInstance();
                break;
            case R.id.settings:
                fragmentForReplacing = TaskMgmtFragment.newInstance();
                break;
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragmentForReplacing)
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                .disallowAddToBackStack()
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "onNavItemSelected: " + item.getTitle());
        replaceFragment(item.getItemId());
        return true;
    }
}
