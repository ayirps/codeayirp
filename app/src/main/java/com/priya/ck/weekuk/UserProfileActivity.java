package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
    }

    @Override
    public void onBackPressed(){
        //startActivity(new Intent(this, MainLoginActivity.class)); //TODO
        finish();
    }
}
