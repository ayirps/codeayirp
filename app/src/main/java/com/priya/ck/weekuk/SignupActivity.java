package com.priya.ck.weekuk;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import helper.Config;
import helper.HelpUtils;
import helper.URLS;
import helper.VolleyRequestHandlerSingleton;
import helper.LogTag;

public class SignupActivity extends AppCompatActivity {

    private String mEmailTxt;
    private String mPassword;
    private String mConfirmPwd;
    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    User mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance();
        mUser = new User();

        /**
         * Handle Forgot Passoword hyperlink click.
         * When a new user clicks Forgot passowrd, an alert should be shown to hel user fetch details
         **/
        TextView contactsupportLinkTxt = (TextView) findViewById(R.id.txt_contactsupport);
        contactsupportLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        contactsupportLinkTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //finish();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri data = Uri.parse("mailto:" + Config.WEEKUK_SUPPORT_EMAIL +
                        "?subject=" + Config.WEEKUK_SUPPORTEMAIL_SUB + "&body=" + Config.WEEKUK_SUPPORTEMAIL_BODY);
                intent.setData(data);
                startActivity(intent);
            }
        });
        //End - handle hyperlink

        /**
         * Handle "back to Sign-in page".
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

        //Handle newuser sign-up
        Button btnSignUP = (Button) findViewById(R.id.btn_NewuserSignUp);
        btnSignUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Handle sign up of a new user
                signupNewUser();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
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
        if (TextUtils.isEmpty(mPassword)  || TextUtils.isEmpty(mConfirmPwd)) {
            userPwd.setError(getResources().getString(R.string.alert_pwd));
            userPwd.requestFocus();
            return;
        }

        if (!TextUtils.equals(mPassword, mConfirmPwd)) {
            userPwd.requestFocus();
            userPwd.setError(getResources().getString(R.string.alert_pwdnotmatch));
            return;
        }

        if (mPassword.length() < 6) {
            userPwd.requestFocus();
            userPwd.setError(getResources().getString(R.string.alert_pwdlength));
            return;
        }

        sendSignUpDetailsToServer();
    }

    protected void signUp(final String email,final String password) {
        Task<AuthResult> authResultTask = mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LogTag.APPLOGIN, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            User u=new User();
                            //u.setName(name);
                            u.setEmail(user.getEmail());
                            mDatabase.getReference("user").child(user.getUid()).setValue(u);
                            //updateUI(user);

                            //starting the Terms&Conditions activity //TODO
                            Intent intent = new Intent(SignupActivity.this,
                                    TermsAndConditionsActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(LogTag.APPLOGIN, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(final FirebaseUser user) {
        if(null!=user){
            mDatabase.getReference("user").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mUser = dataSnapshot.getValue(User.class);
                    mUser.setUid(user.getUid());
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    protected void sendSignUpDetailsToServer(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_SIGNUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            String servResp = obj.getString("status");
                            Log.d(LogTag.GMAILLOGIN, "Response Status" + servResp );
                            if (servResp.equalsIgnoreCase(Config.WEBAPI_RESP_SUCCESS)) {
                                signUp(mEmailTxt, mPassword);
                            } else {
                                Log.d(LogTag.TAG_SIGNUP, "Google login error");
                                HelpUtils.showAlertMessageToUser(SignupActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
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
                params.put("deviceToken", Config.getInstance().getSessionId(getApplicationContext()));
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
        Log.d(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
        VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}


