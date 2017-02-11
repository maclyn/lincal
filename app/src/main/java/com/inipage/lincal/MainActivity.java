package com.inipage.lincal;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.inipage.lincal.ui.DummyFragment;
import com.inipage.lincal.ui.HistoryFragment;
import com.inipage.lincal.ui.TaskMgmtFragment;
import com.inipage.lincal.ui.TodayFragment;
import com.inipage.lincal.ui.TodoFragment;

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

        mNavView.setOnNavigationItemSelectedListener(this);
        replaceFragment(R.id.today);
    }

    public void replaceFragment(int itemId){
        Fragment fragmentForReplacing = DummyFragment.newInstance();
        switch(itemId){
            case R.id.today:
                fragmentForReplacing = TodayFragment.newInstance();
                break;
            case R.id.todo:
                fragmentForReplacing = TodoFragment.newInstance();
                break;
            case R.id.history:
                fragmentForReplacing = HistoryFragment.newInstance();
                break;
            case R.id.tasks:
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
