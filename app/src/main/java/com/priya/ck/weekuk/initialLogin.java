package com.priya.ck.weekuk;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.SignInButton;


public class initialLogin extends AppCompatActivity {
    private Button btnLoginEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_login);

        btnLoginEmail = (Button) findViewById(R.id.btn_loginMain);
        btnLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(initialLogin.this, termsandconditionsActivity.class));
            }
        });

        /*Change the text of google sign in button
        * changing in layout sml didn't suffice*/
        SignInButton signInButton = (SignInButton) findViewById(R.id.gms_signin);
        setButtonText(signInButton, getString(R.string.txt_googlelogin));

        TextView signupLink = (TextView) findViewById(R.id.txtView_Signup);
        signupLink.setMovementMethod(LinkMovementMethod.getInstance());
        SpannableString spannableString = new SpannableString(getString(R.string.txt_accntsignup));
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")));
            }
        };
    }

    /*Changes the text of Google buttons*/
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