package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import Helper.Config;
import Helper.LogTag;
import Helper.URLS;
import Helper.VolleyRequestHandlerSingleton;


public class MainLoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    private Button btnLoginEmail;
    CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Handle AppLogin
        btnLoginEmail = (Button) findViewById(R.id.btn_mainLogin);
        btnLoginEmail.setOnClickListener(this);

        //Initiate FB login
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
    }

    protected void onStart() {
        super.onStart();
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
            case R.id.txtView_Signup:
                startActivity(new Intent(MainLoginActivity.this, SignupActivity.class));
                break;
            case R.id.txt_forgotpwd:
                //TODO - show dialog
                //startActivity(new Intent(MainLoginActivity.this, SignupActivity.class)); //TODO
                break;
            case R.id.btn_mainLogin:
                //TODO - handle login
                startActivity(new Intent(getApplicationContext(), TermsAndConditionsActivity.class));
                break;
        case R.id.gms_signin:
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
                            //if no error in response
                            if (!obj.getBoolean("error")) {
                                Log.i(LogTag.TAG_SIGNUP, obj.getString("message"));
                                //getting the user from the response
                                JSONObject userJson = obj.getJSONObject("user");
                                //starting the Terms&Conditions activity
                                //TODO
                                finish();
                                Intent intent = new Intent(MainLoginActivity.this,
                                        TermsAndConditionsActivity.class);
                                startActivity(intent);
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

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /*Change the text of google sign in button
        * changing in layout sml didn't suffice*/
        SignInButton signInButton = (SignInButton) findViewById(R.id.gms_signin);
        setButtonText(signInButton, getString(R.string.txt_googlelogin));
        signInButton.setSize(SignInButton.SIZE_STANDARD);
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
                                    Intent intent = new Intent(MainLoginActivity.this,
                                    TermsAndConditionsActivity.class);
                                    startActivity(intent);
                                    finish();
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
}