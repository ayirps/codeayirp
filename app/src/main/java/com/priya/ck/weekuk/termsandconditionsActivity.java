package com.priya.ck.weekuk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class termsandconditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsandconditions);

        TextView tncText = (TextView)findViewById(R.id.tnc_textViewFullText);
        tncText.setMovementMethod(new ScrollingMovementMethod());
    }
}
