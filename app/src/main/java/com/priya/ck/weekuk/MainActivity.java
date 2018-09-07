package com.priya.ck.weekuk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.UUID;

import helper.Config;

public class MainActivity extends AppCompatActivity {

    private Boolean firstTime = null;
    private static int SPLASH_TIME = 3000; //3 seconds

    /**
     * Checks if the user is opening the app for the first time.
     * Note that this method should be placed inside an activity and it can be called multiple times.
     * @return boolean
     */
    private boolean isFirstTime() {
        if (firstTime == null) {
            SharedPreferences mPreferences = getApplicationContext().getSharedPreferences(Config.WK_PREFS_NAME, Context.MODE_PRIVATE);
            firstTime = mPreferences.getBoolean("firstTime", true);
            if (firstTime) {
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return firstTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Show tutorial only on very first launch of App after installation
        if (isFirstTime()){ //Check for very first launch of App after installation
            //Method 1
           // String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
            /*Config deviceDetails  = Config.getInstance();
            deviceDetails.setSessionId(android_id);*/
            //Method 2
            String android_id = UUID.randomUUID().toString();
            SharedPreferences mPreferences = this.getSharedPreferences(Config.WK_PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString(Config.WK_PREFS_ID_VAL, android_id);
            editor.commit();

            //Code to start timer and take action after the timer ends
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Moving to next page after the time delay
                     Intent mySuperIntent = new Intent(MainActivity.this, TutorialActivity.class);
                     startActivity(mySuperIntent);
                    /* This 'finish()' is for exiting the app when back button pressed */
                    finish();
                }
            }, SPLASH_TIME);
        }else{
            //Show Main Login page
           Intent intent = new Intent(MainActivity.this,MainLoginActivity.class);
            //Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        }
    }

    public void onStart(){
        super.onStart();
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
