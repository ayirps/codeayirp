package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import Helper.Config;
import Helper.URLS;
import Helper.VolleyRequestHandlerSingleton;
import Helper.LogTag;

public class SignupActivity extends AppCompatActivity {

    private String mEmailTxt;
    private String mPassword;
    private String mConfirmPwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /**
         * Handle Forgot Passoword hyperlink click.
         * When a new user clicks Forgot passowrd, an alert should be shown to hel user fetch details
         **/
        TextView contactsupportLinkTxt = (TextView) findViewById(R.id.txt_contactsupport);
        contactsupportLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        contactsupportLinkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(SignupActivity.this, SignupActivity.class)); //TODO
                finish();
            }
        });
        //End - handle hyperlink

        /**
         * Handle Forgot Passoword hyperlink click.
         * When a new user clicks Forgot passowrd, an alert should be shown to hel user fetch details
         **/
        TextView olduserSignInLinkTxt = (TextView) findViewById(R.id.txt_signin_already);
        olduserSignInLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        olduserSignInLinkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, MainLoginActivity.class));
                finish();
            }
        });
        //End - handle hyperlink

        Button btnSignUP = (Button) findViewById(R.id.btn_NewuserSignUp);
        btnSignUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Handle sign up of a new user
                signupNewUser();
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    /**
     * Param : 1.user email
     *         2.password
     * Function: Validates the user name and pwd, posts a volley request at Sign_up url
     */
    public void signupNewUser() {
        //Get the user entered input details
        EditText userEmail = (EditText) findViewById(R.id.txt_signupemail);
        EditText userPwd = (EditText) findViewById(R.id.txt_signuppassword);
        EditText userConfirmPwd = (EditText) findViewById(R.id.txt_signup_confirmpassword);

        mEmailTxt = userEmail.getText().toString();
        mPassword = userPwd.getText().toString();
        mConfirmPwd = userConfirmPwd.getText().toString();

        //Check if email field is empty
        if (TextUtils.isEmpty(mEmailTxt)) {
            userEmail.setError(getResources().getString(R.string.alert_email));
            userEmail.requestFocus();
            return;
        }

        //check for email id correctness
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(mEmailTxt).matches()) {
            userEmail.setError(getResources().getString(R.string.alert_email));
            userEmail.requestFocus();
            return;
        }

        //Check if password field is empty
        if (TextUtils.isEmpty(mPassword)) {
            userPwd.setError(getResources().getString(R.string.alert_pwd));
            userPwd.requestFocus();
            return;
        }

        //Check if confirm password field is empty
        if (TextUtils.isEmpty(mConfirmPwd)) {
            userConfirmPwd.setError(getResources().getString(R.string.alert_pwd));
            userConfirmPwd.requestFocus();
            return;
        }

        if (!TextUtils.equals(mPassword, mConfirmPwd)) {
            userPwd.requestFocus();
            userPwd.setError(getResources().getString(R.string.alert_pwdnotmatch));
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_SIGNUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);

                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Log.i(LogTag.TAG_SIGNUP, obj.getString("message"));

                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("user");

                                //starting the Terms&Conditions activity
                                //TODO
                                finish();
                                startActivity(new Intent(getApplicationContext(), TermsAndConditionsActivity.class));
                            } else {
                                Log.d(LogTag.TAG_SIGNUP, obj.getString("message"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("deviceToken", Config.getInstance().getSessionId());
                params.put("firstName","");
                params.put("lastName","");
                params.put("loginWith",Config.TYPE_APP_LOGIN);
                params.put("mobile","");
                params.put("password", mPassword);
                params.put("profileName","");
                params.put("profilePicture","");
                params.put("type",Config.OS_TYPE_ANDROID);
                params.put("userName", mEmailTxt);
                Log.i(LogTag.VOLLEYREQTAG, "Request params: " + params.toString());
                return params;
            }
        };

        //post a sign_up request
        Log.i(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
        VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}


