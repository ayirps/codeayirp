package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_termsandconditions);

        TextView tncText = (TextView)findViewById(R.id.tnc_textViewFullText);
        tncText.setMovementMethod(new ScrollingMovementMethod());

        final Button btnAgree = (Button)findViewById(R.id.btn_tnc_Agree);
        btnAgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleTnCAgree();
            }
        });

        final CheckBox cb1 = (CheckBox)findViewById(R.id.tncReadAccept);
        final CheckBox cb2 = (CheckBox)findViewById(R.id.tncAgeAccept);
        CompoundButton.OnCheckedChangeListener checker = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton cb, boolean b) {
                if (cb1.isChecked() && cb2.isChecked()) {
                    //btnAgree.setEnabled(true);
                    btnAgree.setVisibility(View.VISIBLE);
                } else if (btnAgree.isEnabled()) {
                    btnAgree.setVisibility(View.INVISIBLE);
                    //btnAgree.setEnabled(false);
                }
            }
        };
        cb1.setOnCheckedChangeListener(checker);
        cb2.setOnCheckedChangeListener(checker);

        /*Button btnCancel = (Button)findViewById(R.id.btn_tnc_Cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                Intent intent = new Intent(TermsAndConditionsActivity.this,
                        MainLoginActivity.class);
                startActivity(intent);
            }
        });*/
    }

    @Override
    public void onBackPressed() {
           Intent a = new Intent(Intent.ACTION_MAIN);
            a.addCategory(Intent.CATEGORY_HOME);
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(a);
     }

    /**
     * Check if the user has clicked the checkboxes to accept T&C and age limit
     */
    protected void handleTnCAgree(){
        /*CheckBox checkboxAge,checkboxReadAgree;
        checkboxReadAgree = (CheckBox)findViewById(R.id.tncReadAccept);
        checkboxAge = (CheckBox)findViewById(R.id.tncAgeAccept);*/

        /*if((checkboxReadAgree.isChecked())&& (checkboxAge.isChecked())){
            Button btnAgree = (Button)findViewById(R.id.btn_tnc_Agree);
            btnAgree.setEnabled(true);*/
            Intent intent = new Intent(TermsAndConditionsActivity.this,
                    UserProfileActivity.class);
            startActivity(intent);
        /*}else{
            VolleyRequestHandlerSingleton.showAlertMessageToUser
                    (TermsAndConditionsActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_tnc_chkbox_select));
        }*/
    }
}
