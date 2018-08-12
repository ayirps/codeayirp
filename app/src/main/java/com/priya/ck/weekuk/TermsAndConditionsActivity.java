package com.priya.ck.weekuk;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsandconditions);

        TextView tncText = (TextView)findViewById(R.id.tnc_textViewFullText);
        tncText.setMovementMethod(new ScrollingMovementMethod());

        Button btnAgree = (Button)findViewById(R.id.btn_tnc_Agree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleAgree();
            }
        });
    }

    /**
     * Check if the user has clicked the checkboxes to accept T&C and age limit
     */
    protected void handleAgree(){
        CheckBox checkboxAge,checkboxReadAgree;
        checkboxReadAgree = (CheckBox)findViewById(R.id.tncReadAccept);
        checkboxAge = (CheckBox)findViewById(R.id.tncAgeAccept);
        if(!checkboxReadAgree.isChecked()){
            checkboxReadAgree.setError(getResources().getString(R.string.alert_email));
            checkboxReadAgree.requestFocus();
        }
        if(!checkboxAge.isChecked()){
            checkboxAge.setError(getResources().getString(R.string.alert_email));
            checkboxAge.requestFocus();
        }

        //TODO
        //Start ProfileActivity page

    }
}
