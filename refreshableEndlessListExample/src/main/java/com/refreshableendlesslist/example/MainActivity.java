package com.refreshableendlesslist.example;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity
{
    private Fragment tweetsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        FragmentManager fragmentManager = getFragmentManager();
        tweetsFragment = fragmentManager.findFragmentById(R.id.main_fragment_container);
        if (tweetsFragment == null) {
            tweetsFragment = new TweetsFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.main_fragment_container, tweetsFragment);
            fragmentTransaction.commit();
        }
    }
}
