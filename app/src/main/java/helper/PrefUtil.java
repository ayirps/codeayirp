package helper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
* Created by User on 07/09/2018.
*/

public class PrefUtil {
    private Activity activity;

    // Constructor
    public PrefUtil(Activity activity) {
        this.activity = activity;
    }

    public void saveAccessToken(String token,String provider) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        if(provider.equals("fb"))
            editor.putString("fb_access_token", token);
        else if(provider.equals("google"))
            editor.putString("google_access_token", token);
        editor.apply(); // This line is IMPORTANT !!!
    }


    public String getToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        return prefs.getString("fb_access_token", null);
    }

    public void clearToken() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply(); // This line is IMPORTANT !!!
    }
}
