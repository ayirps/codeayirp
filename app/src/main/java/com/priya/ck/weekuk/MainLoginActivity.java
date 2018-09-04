package com.priya.ck.weekuk;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import helper.Config;
import helper.HelpUtils;
import helper.LogTag;
import helper.URLS;
import helper.VolleyRequestHandlerSingleton;

public class MainLoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private Button btnLoginEmail;
    CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    User mUser;
    String mSessionId;

    private void getDeviceUUID() {
        SharedPreferences mPreferences = getApplicationContext().getSharedPreferences(Config.WK_PREFS_NAME, Context.MODE_PRIVATE);
        mSessionId = mPreferences.getString(Config.WK_PREFS_ID_VAL, null);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_login);

        getDeviceUUID();

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase=FirebaseDatabase.getInstance();
        mUser = new User();

        //Handle AppLogin
        btnLoginEmail = (Button) findViewById(R.id.btn_mainLogin);
        btnLoginEmail.setOnClickListener(this);

        //Initiate FB login
       /* com.facebook.login.widget.LoginButton loginOrLogoutButton = (com.facebook.login.widget.LoginButton ) findViewById(R.id.login_fbbutton);
        loginOrLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser == null) { //do something
                } else { //do something else }*/
        handleFacebookLogin();

        //Handle Gmail login option
        handleGmailLogin();

        //Handle Hyperlinks
        /**
        * Handle SignUp hyperlink click.
        * When a new user clicks SignUp, the user details has to be fetched and registered
        **/
        TextView signupLinkTxt = (TextView) findViewById(R.id.txtView_Signup);
        signupLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        signupLinkTxt.setOnClickListener(this); // handle hyperlink for SignUp -1

        /**
         * Handle Forgot Passoword hyperlink click.
         * When a new user clicks Forgot passowrd, an alert should be shown to hel user fetch details
         **/
        TextView forgotPwdLinkTxt = (TextView) findViewById(R.id.txt_forgotpwd);
        forgotPwdLinkTxt.setMovementMethod(LinkMovementMethod.getInstance());
        forgotPwdLinkTxt.setOnClickListener(this);//handle hyperlink - 2

        //Check if device is connected to internet
        boolean checkInternet = HelpUtils.isOnline(getApplicationContext());
        if(!checkInternet){
            HelpUtils.showAlertMessageToUser(MainLoginActivity.this,
                    getString(R.string.app_name),getString(R.string.txt_alert_check_internet_status));
            finish();
        }
    }

    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtView_Signup: //Handle new sign-up
                startActivity(new Intent(MainLoginActivity.this, SignupActivity.class));
                break;
            case R.id.txt_forgotpwd: //Handle forgot pwd
                Log.d(LogTag.FORGOTPWD, "Forgot Pwd");
                showDialog(Config.ALERT_DIALOG_FORGOTPWD);
                break;
            case R.id.btn_mainLogin: //Handle app login
                handleAppLogin();
                break;
        case R.id.gms_signin: //Handle google sign-in
                signInGmail();
                break;
            /*case R.id.sign_out_button:
                onSignOutClicked();
                break;*/
        }
    }
    @Override
    public void onBackPressed(){
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    /**
     * Creates alert dialog for Forgot Password, where user enters registered email
     * @param id
     * @return
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Log.d(LogTag.FORGOTPWD, "OnCreate Dialog Start");
        AlertDialog dialogDetails = null;

        switch (id) {
            case Config.ALERT_DIALOG_FORGOTPWD:
                LayoutInflater inflater = LayoutInflater.from(this);
                View dialogview = inflater.inflate(R.layout.alertdialog_forgot_password, null);

                AlertDialog.Builder dialogbuilder = new AlertDialog.Builder(this);
                //dialogbuilder.setTitle(getString(R.string.txt_forgotpwd_dialogTitle));
                dialogbuilder.setView(dialogview);
                // disallow cancel of AlertDialog on click of back button and outside touch
                dialogbuilder.setCancelable(false);

                dialogDetails = dialogbuilder.create();
                dialogDetails.getWindow().setLayout(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
                dialogDetails.getWindow().setGravity(Gravity.CENTER);
                dialogDetails.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogDetails.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                break;
        }

        return dialogDetails;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        Log.d(LogTag.FORGOTPWD, "onPrepareDialog Start");
        switch (id) {
            case Config.ALERT_DIALOG_FORGOTPWD:
                final AlertDialog alertDialog = (AlertDialog) dialog;
                Button okbutton = (Button) alertDialog
                        .findViewById(R.id.btn_forgotpassword_ok);
                Button cancelbutton = (Button) alertDialog
                        .findViewById(R.id.btn_forgotpassword_cancel);

                final EditText userEmail = (EditText) alertDialog
                        .findViewById(R.id.txt_forgotpwd_email);

                okbutton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String valUserEmail = userEmail.getText().toString();
                        Log.d(LogTag.FORGOTPWD, "OK Clicked Email:" + valUserEmail);
                        //Check if email field is empty
                        if (TextUtils.isEmpty(valUserEmail) ||
                                !android.util.Patterns.EMAIL_ADDRESS.matcher(valUserEmail).matches()) {
                            userEmail.setError(getResources().getString(R.string.alert_email));
                            userEmail.requestFocus();
                            return;
                        }
                        alertDialog.dismiss();
                        sendForgotPasswordRequest(valUserEmail);
                    }
                });

               cancelbutton.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       alertDialog.dismiss();
                   }
               });
            break;
        }
    }

    /**
     * Send forgot password request to the webserver
     * @param userEmail
     */
    protected void sendForgotPasswordRequest(final String userEmail){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_FORGOTPWD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);
                            //if no error in response
                            String servResp = obj.getString("status");
                            if (servResp.equalsIgnoreCase(Config.WEBAPI_RESP_SUCCESS)) {
                                // If you have array
                                String resVal = obj.getString("result");
                                HelpUtils.showAlertMessageToUser(MainLoginActivity.this,
                                        getString(R.string.app_name),resVal);
                                Log.d(LogTag.GMAILLOGIN, "Response Status" + servResp + resVal);
                            } else{
                                HelpUtils.showAlertMessageToUser(MainLoginActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
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
                params.put("userName", userEmail);
                Log.i(LogTag.VOLLEYREQTAG, "Request params: " + params.toString());
                return params;
            }
        };
        //post a sign_up request
        Log.i(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
        VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
    /**
     * Handle Login button when user enters valid Email and password
     * if success, show terms and conditions page
     * if error, display an alert to user
     */
    protected void handleAppLogin() {

        Log.d(LogTag.APPLOGIN, "Before posting request for login");
        //Get the user entered input details
        final EditText userEmail = (EditText) findViewById(R.id.txt_loginemail);
        EditText userPwd = (EditText) findViewById(R.id.txt_emailpassword);

        final String loginEmailTxt = userEmail.getText().toString();
        final String loginPassword = userPwd.getText().toString();

        //Check if email field is empty
        if (TextUtils.isEmpty(loginEmailTxt)) {
            userEmail.setError(getResources().getString(R.string.alert_email));
            userEmail.requestFocus();
            return;
        }

        //check for email id correctness
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(loginEmailTxt).matches()) {
            userEmail.setError(getResources().getString(R.string.alert_email));
            userEmail.requestFocus();
            return;
        }

        //Check if password field is empty
        if (TextUtils.isEmpty(loginPassword)) {
            userPwd.setError(getResources().getString(R.string.alert_pwd));
            userPwd.requestFocus();
            return;
        }

        sendAppLoginDetailsToServer(loginEmailTxt,loginPassword);
    }

    /**
     * Authenticate with Firebase server
     * @param email
     * @param password
     */
    protected void appSignInwithFirebase(final String email,final String password,final int pageStatusVal){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LogTag.APPLOGIN, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            System.out.println("Login success..........");
                            //updateUI(user);
                            finish();
                            pageNavigate(pageStatusVal);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(LogTag.APPLOGIN, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainLoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    /**
     * Send App login details to server
     */
    protected void sendAppLoginDetailsToServer(final String loginEmailTxt,final String loginPassword){
        Log.d(LogTag.APPLOGIN, "Before posting request for login");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            Log.d(LogTag.VOLLEYREQTAG, "After response");
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);
                            //if no error in response
                            String servResp = obj.getString("status");
                            if (servResp.equalsIgnoreCase(Config.WEBAPI_RESP_SUCCESS)) {
                                // If you have array
                                JSONObject resultObj = obj.getJSONObject("result"); // Here you will get the Array
                                int pageStatusVal = resultObj.getInt("pageStatus");
                                Log.d(LogTag.GMAILLOGIN, "Response Status" + servResp + pageStatusVal);
                                appSignInwithFirebase(loginEmailTxt,loginPassword,pageStatusVal);
                            } else {
                                Log.d(LogTag.APPLOGIN, obj.getString("Error in Ap Login Response"));
                                HelpUtils.showAlertMessageToUser(MainLoginActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_namepwd_err));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(LogTag.VOLLEYREQTAG, "Error response received");
                        // As of f605da3 the following should work
                        NetworkResponse response = error.networkResponse;
                        if (error instanceof ServerError && response != null) {
                            try {
                                String res = new String(response.data,
                                        HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                                Log.d(LogTag.VOLLEYREQTAG, "Error response" + res);
                                // Now you can use any deserializer to make sense of data
                                JSONObject obj = new JSONObject(res);
                            } catch (UnsupportedEncodingException e1) {
                                // Couldn't properly decode data to string
                                Log.d(LogTag.VOLLEYREQTAG, "Error response.. Print Stack trace");
                                e1.printStackTrace();
                            } catch (JSONException e2) {
                                // returned data is not JSONObject?
                                e2.printStackTrace();
                            }
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("deviceToken", mSessionId);
                params.put("firstName","");
                params.put("lastName","");
                params.put("loginWith",Config.TYPE_APP_LOGIN);
                params.put("mobile","");
                params.put("password", loginPassword);
                params.put("profileName","");
                params.put("profilePicture","");
                params.put("type",Config.OS_TYPE_ANDROID);
                params.put("userName", loginEmailTxt);
                Log.d(LogTag.VOLLEYREQTAG, "Request params: " + params.toString());
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                String creds = String.format("%s:%s","username","password");
                String auth = "Basic " + Base64.encodeToString(creds.getBytes(), Base64.DEFAULT);
                params.put("Authorization", auth);
                return params;

            }
        };
        //post a sign_up request
        Log.d(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
        VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    private void updateUI(final FirebaseUser user) {
        if(null != user){
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(LogTag.GMAILLOGIN, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == Config.RC_GMAIL_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    firebaseAuthWithGoogle(account);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.d(LogTag.GMAILLOGIN, "Google sign in failed", e);
                    // [START_EXCLUDE]
                    //updateUI(null);
                    // [END_EXCLUDE]
                }
            } else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LogTag.GMAILLOGIN, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Hanlde FB login
     */
    protected void handleFacebookLogin(){
        Log.d(LogTag.FBLOGIN, "Start");
        mCallbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_fbbutton);
        loginButton.setReadPermissions(Arrays.asList(Config.FB_EMAIL));
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(LogTag.FBLOGIN, "Logged In");
                // App code to fetch details on Successful FB login
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                // App code
                Log.d(LogTag.FBLOGIN, "Cancelled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d(LogTag.FBLOGIN, "Error in FB Login");
            }
        });
    }
    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(LogTag.FBLOGIN, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LogTag.FBLOGIN, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null)
                                sendFBLoginDetails(user);
                            else
                                Log.d(LogTag.FBLOGIN, "signInWithCredential:NULL data");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(LogTag.FBLOGIN, "signInWithCredential:failure", task.getException());
                            HelpUtils.showAlertMessageToUser
                                    (MainLoginActivity.this,getString(R.string.app_name),
                                            getString(R.string.txt_alert_user_response_err));
                        }
                    }
                });
    }
// [END auth_with_facebook]
    /**
     * Send FB details to the MySql server for storing user details
     * @param user
     */
    protected void sendFBLoginDetails(final FirebaseUser user){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_SIGNUP,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //converting response to json object
                            JSONObject obj = new JSONObject(response);
                            String servResp = obj.getString("status");
                            if (servResp.equalsIgnoreCase(Config.WEBAPI_RESP_SUCCESS)) {
                                // If you have array
                                JSONObject resultObj = obj.getJSONObject("result"); // Here you will get the Array
                                int pageStatusVal = resultObj.getInt("pageStatus");
                                Log.d(LogTag.GMAILLOGIN, "Response Status" + servResp + pageStatusVal);
                                pageNavigate(pageStatusVal);
                            }  else {
                                Log.d(LogTag.TAG_SIGNUP, obj.getString("message"));
                                HelpUtils.showAlertMessageToUser(MainLoginActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
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
                params.put("deviceToken", mSessionId);
                params.put("firstName","");
                params.put("lastName","");
                params.put("mobile","");
                params.put("email", user.getEmail());
                params.put("profileName","");
                params.put("profilePicture","");
                params.put("loginWith",Config.TYPE_FBLOGIN);
                params.put("type",Config.OS_TYPE_ANDROID);
                params.put("userName", user.getDisplayName());
                params.put("fbUniqueId",user.getUid());
                Log.i(LogTag.VOLLEYREQTAG, "Request params: " + params.toString());
                return params;
            }
        };
        //post a sign_up request
        Log.i(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
        VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
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

    protected void handleGmailLogin(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //mGoogleApiClient.clearDefaultAccountAndReconnect();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /*Change the text of google sign in button
        * changing in layout sml didn't suffice*/
        SignInButton signInButton = (SignInButton) findViewById(R.id.gms_signin);
        setButtonText(signInButton, getString(R.string.txt_googlelogin));
        //signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(this);
    }

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(LogTag.GMAILLOGIN, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(LogTag.GMAILLOGIN, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user != null)
                                sendAndSaveGoogleSignDetails(user);
                            else
                                Log.d(LogTag.GMAILLOGIN, "firebaseAuthWithGoogle:" + "NULL data");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(LogTag.GMAILLOGIN, "signInWithCredential:failure", task.getException());
                            //Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            HelpUtils.showAlertMessageToUser(MainLoginActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
                            //updateUI(null);
                        }
                    }
                });
    }
// [END auth_with_google]

    /**
     * Call Webservice api to send details of gmail login to the server
     */
    private void sendAndSaveGoogleSignDetails(FirebaseUser user) {
        Log.d(LogTag.GMAILLOGIN, "sendAndSaveGoogleSignDetails:" + "Start");
        if(user != null) {
            final String personName = user.getDisplayName();
            final String email = user.getEmail();

            Log.d(LogTag.GMAILLOGIN, "Name: " + personName + ", email: " + email);

           StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_LOGIN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                String servResp = obj.getString("status");
                                if (servResp.equalsIgnoreCase(Config.WEBAPI_RESP_SUCCESS)) {
                                    // If you have array
                                    JSONObject resultObj = obj.getJSONObject("result"); // Here you will get the Array
                                    int pageStatusVal = resultObj.getInt("pageStatus");
                                    Log.d(LogTag.GMAILLOGIN, "Response Status" + servResp + pageStatusVal);
                                    pageNavigate(pageStatusVal);
                                } else {
                                    Log.d(LogTag.TAG_SIGNUP, "Google login error");
                                    HelpUtils.showAlertMessageToUser(MainLoginActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
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
                    params.put("deviceToken", mSessionId);
                    params.put("firstName","");
                    params.put("lastName","");
                    params.put("mobile","");
                    params.put("mailId", email);
                    params.put("profileName","");
                    params.put("profilePicture","");
                    params.put("loginWith",Config.TYPE_GMSLOGIN);
                    params.put("type",Config.OS_TYPE_ANDROID);
                    params.put("userName", personName);
                    Log.i(LogTag.VOLLEYREQTAG, "Request params: " + params.toString());
                    return params;
                }
            };
            //post a Gmail sign in request
            Log.i(LogTag.VOLLEYREQTAG, "Request body: " + stringRequest);
            VolleyRequestHandlerSingleton.getInstance(this).addToRequestQueue(stringRequest);
        }
        else{
            Log.d(LogTag.GMAILLOGIN, "sendAndSaveGoogleSignDetails:" + "NULL data");
        }
    }
    private void signInGmail() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, Config.RC_GMAIL_SIGN_IN);
    }

    /**
     * Decide and navigate to the page according to the pgeStatus received from the server response
     * @param pageStatus
     * 2- registered, 3- t&c, 4-profile, 5-letscook 6-chat 7-rating
     */
    protected void pageNavigate(int pageStatus){
        switch(pageStatus) {
            case 1: {
                break;
            }
            case 2: {
                //starting the Terms&Conditions activity
                Intent intent = new Intent(MainLoginActivity.this,
                        TermsAndConditionsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 3: {
                //starting Terms&Conditions activity
                Intent intent = new Intent(MainLoginActivity.this,
                        TermsAndConditionsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 4: {
                //TODO
                //starting UserProfileActivity activity
                Intent intent = new Intent(MainLoginActivity.this,
                        UserProfileActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 5: {
                //TODO
                //starting Terms&Conditions activity
                Intent intent = new Intent(MainLoginActivity.this,
                        TermsAndConditionsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 6: {
                //TODO
                //starting Terms&Conditions activity
                Intent intent = new Intent(MainLoginActivity.this,
                        TermsAndConditionsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case 7: {
                //TODO
                //starting Terms&Conditions activity
                Intent intent = new Intent(MainLoginActivity.this,
                        TermsAndConditionsActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            default:{
                HelpUtils.showAlertMessageToUser
                        (MainLoginActivity.this, getString(R.string.app_name),
                                getString(R.string.txt_alert_user_namepwd_err));
                break;
        }
        }
    }
}