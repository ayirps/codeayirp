package com.priya.ck.weekuk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import Helper.Config;

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
            SharedPreferences mPreferences = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
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
        if (true/*isFirstTime()*/){

            //Store device if Singleton class for use in all activities
            String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            Config deviceDetails  = Config.getInstance();
            deviceDetails.setSessionId(android_id);

            //Code to start timer and take action after the timer ends
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //Do any action here. Now we are moving to next page
                    Intent mySuperIntent = new Intent(MainActivity.this, TutorialActivity.class);
                    startActivity(mySuperIntent);
                    /* This 'finish()' is for exiting the app when back button pressed */
                    finish();
                }
            }, SPLASH_TIME);
        }else{

        }
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}
