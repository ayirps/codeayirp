package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;


public class MainLoginActivity extends AppCompatActivity {
    private Button btnLoginEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_login);

        btnLoginEmail = (Button) findViewById(R.id.btn_mainLogin);
        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainLoginActivity.this, TermsAndConditionsActivity.class)); //TODO
                startActivity(new Intent(getApplicationContext(), TermsAndConditionsActivity.class));
            }
        });

        /*Change the text of google sign in button
        * changing in layout sml didn't suffice*/
        SignInButton signInButton = (SignInButton) findViewById(R.id.gms_signin);
        setButtonText(signInButton, getString(R.string.txt_googlelogin));

        /**
        * Handle SignUp hyperlink click.
        * When a new user clicks SignUp, the user details has to be fetched and registered
        **/
        TextView signupLinkTxt = (TextView) findViewById(R.id.txtView_Signup);
        signupLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        signupLinkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(MainLoginActivity.this, SignupActivity.class));
                finish();
            }
        });
        //End - handle hyperlink for SignUp -1

        /**
         * Handle Forgot Passoword hyperlink click.
         * When a new user clicks Forgot passowrd, an alert should be shown to hel user fetch details
         **/
        TextView forgotPwdLinkTxt = (TextView) findViewById(R.id.txt_forgotpwd);
        forgotPwdLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        forgotPwdLinkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //startActivity(new Intent(MainLoginActivity.this, SignupActivity.class)); //TODO
                finish();
            }
        });
        //End - handle hyperlink - 2
    }

    /**
    * Changes the text of Google buttons
    **/
    protected void setButtonText(SignInButton signInButton, String buttonText) {
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                tv.setPadding(0, 0, 50, 0);
                return;
            }

        }
    }
}