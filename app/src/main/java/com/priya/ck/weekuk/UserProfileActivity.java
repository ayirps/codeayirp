package com.priya.ck.weekuk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.xw.repo.BubbleSeekBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import helper.Config;
import helper.HelpUtils;
import helper.LogTag;
import helper.URLS;
import helper.VolleyRequestHandlerSingleton;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private BubbleSeekBar radiusSeekBar;
    private double mUserDistanceSelection = 0;
    String mUserName;
    String mGender = "";
    String mFoodPref = "";
    String mCookSkill = "";
    double mLatitude = 0.0;
    double mLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        radiusSeekBar = (BubbleSeekBar)findViewById(R.id.radiusSeekBar);

        ImageView mIcon = findViewById(R.id.ivProfile);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.com_facebook_profile_picture_blank_square);
        RoundedBitmapDrawable mDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        mDrawable.setColorFilter(ContextCompat.getColor(UserProfileActivity.this, R.color.colorAccent), PorterDuff.Mode.DST_OVER);
        mIcon.setImageDrawable(mDrawable);
        mDrawable.setCircular(true);

        //Choose Gender
        RadioGroup rg = (RadioGroup) findViewById(R.id.radioGroup_userprof_gender);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioButton_userprof_male:
                        // do operations specific to this selection
                        mGender = "Male";
                        break;
                    case R.id.radioButton_userprof_female:
                        // do operations specific to this selection
                        mGender = "Female";
                        break;
                    }
            }
        });

        //Choose FoodPreference
        RadioGroup rg_foodpref = (RadioGroup) findViewById(R.id.radioGroup_userprof_food);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioButton_userprof_food_v:
                        // do operations specific to this selection
                        mFoodPref = "Vegetarian";
                        break;
                    case R.id.radioButton_userprof_food_nv:
                        // do operations specific to this selection
                        mFoodPref = "NonVegetarian";
                        break;
                    case R.id.radioButton_userprof_food_both:
                        // do operations specific to this selection
                        mFoodPref = "Both";
                        break;
                }
            }
        });
        //Choose CookingSkill
        RadioGroup rg_cookSkill = (RadioGroup) findViewById(R.id.radioGroup_userprof_cookskill);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.radioButton_userprof_cookskill_begin:
                        // do operations specific to this selection
                        mCookSkill = "Beginner";
                        break;
                    case R.id.radioButton_userprof_cookskill_mid:
                        // do operations specific to this selection
                        mFoodPref = "MidLevel";
                        break;
                    case R.id.radioButton_userprof_cookskill_chef:
                        // do operations specific to this selection
                        mFoodPref = "Chef";
                        break;
                }
            }
        });

        ImageButton openMap = findViewById(R.id.btn_googlemap_placemarker);
        openMap.setOnClickListener(this);
    }

    @Override
    public void onBackPressed(){
        /*Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);*/
        Intent intent = new Intent(UserProfileActivity.this,MainLoginActivity.class);
        startActivity(intent);
    }

    public void onClick(View view) {
        Log.d(LogTag.USERPROFILE, "UserProfile:OnClick Start");
        switch(view.getId()){
            case R.id.btn_googlemap_placemarker:{
                Intent intent = new Intent(UserProfileActivity.this,CurrentLocationActivity.class);
                startActivityForResult(intent,Config.LOCATION_GMAP);
                break;
            }
            case R.id.btn_userprofile_save:{
                handleUserProfileSave();
                break;
            }
        }
        Log.d(LogTag.USERPROFILE, "UserProfile:OnClick End");
    }

    private void handleUserProfileSave(){
        Log.d(LogTag.USERPROFILE, "UserProfile:handleUserProfileSave Start");
        //1.Get the user entered name
        EditText userNameET = (EditText) findViewById(R.id.et_userprofile_name);
        mUserName = userNameET.getText().toString();
        //Check if email field is empty
        if (TextUtils.isEmpty(mUserName)) {
            userNameET.setError(getResources().getString(R.string.alert_email));
            userNameET.requestFocus();
            return;
        }

        //2.Gender & 4.Distance
        if(mGender.equals("") || (mUserDistanceSelection == 0)){
            HelpUtils.showAlertMessageToUser(UserProfileActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
            return;
        }

        sendDetailsToServer();
        Log.d(LogTag.USERPROFILE, "UserProfile:handleUserProfileSave End");
    }
private String mLocalAddress = "";
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LogTag.USERPROFILE, "UserProfile:onActivityResult Start");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Config.LOCATION_GMAP) {
            if(resultCode == RESULT_OK) {
                Intent intent = getIntent();
                //3.Location
                intent.getDoubleExtra(Config.CURR_LOCATION_LAT,mLatitude);
                intent.getDoubleExtra(Config.CURR_LOCATION_LONG,mLongitude);
                if((mLatitude== 0.0) || (mLongitude == 0.0)){
                    HelpUtils.showAlertMessageToUser(UserProfileActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_response_err));
                    return;
                }
                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(this, Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();                }

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                EditText userNameET = (EditText) findViewById(R.id.editText_userprof_location);
                mLocalAddress = city + ','+ postalCode;
                userNameET.setText(mLocalAddress);
            }
        }
        Log.d(LogTag.USERPROFILE, "UserProfile:onActivityResult End");
    }

    /**
     * Handle user selection of the distance to receive posts
     */
    private void initRadius() {
        radiusSeekBar.getConfigBuilder()
                .progress(10)
                .sectionCount(4)
                .showSectionText()
                .sectionTextSize(12)
                .thumbTextSize(12)
                .bubbleTextSize(12)
                .showSectionMark()
                .seekBySection()
                .autoAdjustSectionMark()
                .sectionTextPosition(BubbleSeekBar.TextPosition.BELOW_SECTION_MARK)
                .build();

        radiusSeekBar.setCustomSectionTextArray(new BubbleSeekBar.CustomSectionTextArray() {
            @NonNull
            @Override
            public SparseArray<String> onCustomize(int sectionCount, @NonNull SparseArray<String> array) {
                array.clear();
                array.put(0, "1km");
                array.put(1, "2kms");
                array.put(2, "3kms");
                array.put(3, "5kms");
                array.put(4, "10kms");
                return array;
            }
        });

        radiusSeekBar.setOnProgressChangedListener(new BubbleSeekBar.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {
                Log.d(LogTag.USERPROFILE, "onProgressChanged: " + progress);
                mUserDistanceSelection = progressFloat;
            }

            @Override
            public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }

            @Override
            public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

            }
        });
    }

    private void sendDetailsToServer(){
        Log.d(LogTag.USERPROFILE, "Before posting request for profile");
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.URL_REGISTER,
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
                                //TODO - integrate BrowseFeed
                            } else {
                                Log.d(LogTag.APPLOGIN, obj.getString("Error in Ap Login Response"));
                                HelpUtils.showAlertMessageToUser(UserProfileActivity.this,getString(R.string.app_name),getString(R.string.txt_alert_user_namepwd_err));
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
                params.put("deviceToken", Config.getInstance().getSessionId(getApplicationContext()));
                params.put("profileName",mUserName);
                params.put("gender",mGender);
                params.put("location",mLocalAddress);
                params.put("radius",Double.toString(mUserDistanceSelection));
                params.put("homeLat", Double.toString(mLatitude));
                params.put("homeLng",Double.toString(mLongitude));
                params.put("foodPreference",mFoodPref);
                params.put("cookingSkill",mCookSkill);
                /*params.put("favFoods","biriyani,dosa");
                params.put("hobbies",Config.OS_TYPE_ANDROID);
                params.put("profilePicture",Config.OS_TYPE_ANDROID);
                params.put("kitchenPicture",Config.OS_TYPE_ANDROID);
                params.put("pageStatus",Config.OS_TYPE_ANDROID);*/
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
}
