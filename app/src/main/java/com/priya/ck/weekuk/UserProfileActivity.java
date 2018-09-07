package com.priya.ck.weekuk;

import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
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

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import helper.Config;
import helper.HelpUtils;
import helper.LogTag;
import helper.URLS;
import helper.VolleyRequestHandlerSingleton;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener{
    public static final int USERPROFILEHOBBY_REQUEST_CODE = 26;
    public static final int USERPROFILEFOOD_REQUEST_CODE = 4;
    public static final int GOOGLEMAP_REQUEST_CODE = 10;
    private BubbleSeekBar radiusSeekBar;
    private double mUserDistanceSelection = 0;
    String mUserName;
    String mGender = "";
    String mFoodPref = "";
    String mCookSkill = "";
    double mLatitude = 0.0;
    double mLongitude = 0.0;
    ImagePicker mKitchenPicSelector;
    String mKitchenPic="";
    String mHobbyStr ="";
    String mFavFoodStr ="";
    private String mLocalAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LogTag.USERPROFILE, "UserProfile:OnCreate Start");
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

        //Open Google maps and choose location
        EditText edLocation = findViewById(R.id.editText_userprof_location);
        edLocation.setOnClickListener(this);
        ImageButton openMap = findViewById(R.id.btn_googlemap_placemarker);
        openMap.setOnClickListener(this);

        ImageView kitchenPic = findViewById(R.id.kitchenPic);
        kitchenPic.setOnClickListener(this);

        EditText et_hobby = findViewById(R.id.textView_userprof_Hobbies);
        et_hobby.setOnClickListener(this);

        EditText et_favFood = findViewById(R.id.textView_userprof_favouritefood);
        et_favFood.setOnClickListener(this);

        Button btn_Save = findViewById(R.id.btn_userprofile_save);
        btn_Save.setOnClickListener(this);
        //Data will be broadcasted when user fills tags for Hobbies and FavouriteFood
        listenForUserData();
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
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
            case R.id.editText_userprof_location:
            case R.id.btn_googlemap_placemarker:{
                Intent intent = new Intent(UserProfileActivity.this,CurrentLocationActivity.class);
                startActivityForResult(intent,GOOGLEMAP_REQUEST_CODE);//Config.LOCATION_GMAP);
                break;
            }
            case R.id.btn_userprofile_save:{
                handleUserProfileSave();
                break;
            }
            case R.id.kitchenPic:{
                mKitchenPicSelector = new ImagePicker.Builder(UserProfileActivity.this)
                        .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                        .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                        .directory(ImagePicker.Directory.DEFAULT)
                        .extension(ImagePicker.Extension.JPG)
                        .scale(600, 600)
                        .allowMultipleImages(false)
                        .enableDebuggingMode(true)
                        .build();
                break;
            }
            case R.id.textView_userprof_Hobbies:{
                Intent intent = new Intent(UserProfileActivity.this,UserProfileHobbyActivity.class);
                startActivityForResult(intent,Config.REQCODE_USERPROF_HOBBY);
                // custom dialog
                //showHobbyDialog();
                break;
            }
            case R.id.textView_userprof_favouritefood:{
                Intent intent = new Intent(UserProfileActivity.this,UserProfileFavFoodActivity.class);
                startActivityForResult(intent,Config.REQCODE_USERPROF_FAVFOOD);
                break;
            }
        }
        Log.d(LogTag.USERPROFILE, "UserProfile:OnClick End");
    }



    /**
     * Wait for call from another actitivity
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LogTag.USERPROFILE, "UserProfile:onActivityResult Start");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLEMAP_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                /*Intent intent = data;
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
                EditText userNameLocationET = (EditText) findViewById(R.id.editText_userprof_location);
                mLocalAddress = city + ','+ postalCode;
                userLocationET.setText(mLocalAddress);*/
            }
        }else  if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            //call the method 'getImageFilePath(Intent data)' even if compression is set to false
            String filePath = mPaths.get(0);
            //TODO
            if (filePath != null) {//filePath will return null if compression is set to true
                Bitmap selectedImage = BitmapFactory.decodeFile(filePath);
                ImageView kitchenPic = findViewById(R.id.kitchenPic);
                kitchenPic.setImageBitmap(selectedImage);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageBytes = baos.toByteArray();
                mKitchenPic = Base64.encodeToString(imageBytes, Base64.DEFAULT);
            }
        }/*else if(requestCode == Config.REQCODE_USERPROF_HOBBY && resultCode == RESULT_OK){
            Intent intent = data;
            ArrayList<String> hobbyTags = new ArrayList<String>();
            hobbyTags = intent.getStringArrayListExtra(Config.USERPROF_TAG_HOBBYTAG);
            int i= hobbyTags.size();
            while(i >= 0) {
                i--;
                mHobbyStr.concat(hobbyTags.get(i));
                mHobbyStr.concat(",");
            }
        }else if(requestCode == Config.REQCODE_USERPROF_FAVFOOD && resultCode == RESULT_OK){
            Intent intent = getIntent();
            ArrayList<String> favfoodTags = new ArrayList<String>();
            favfoodTags = intent.getStringArrayListExtra(Config.USERPROF_TAG_FAVFOODTAG);
            int i= favfoodTags.size();
            while(i >= 0) {
                i--;
                mFavFoodStr.concat(favfoodTags.get(i));
                mFavFoodStr.concat(",");
            }
        }*/
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
                params.put("favFoods",mFavFoodStr);
                params.put("hobbies",mHobbyStr);
                params.put("profilePicture",mKitchenPic);
                params.put("kitchenPicture",mKitchenPic);
                //params.put("pageStatus",Config.OS_TYPE_ANDROID);
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

    private BroadcastReceiver mMessageReceiver;

    /** Note!!!
     * startActivityForResult seems to not work because OnActivityResult is never getting called.
     * LocalBroadcastReceiver is created as a workaround for this problem.
     *  of receving data from tag container
     */
    protected void listenForUserData(){
        mMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Get extra data included in the Intent
                String messageSrc = intent.getStringExtra("Source");
                Log.d(LogTag.USERPROFILE, "Got message: ");
                if (messageSrc.equals(Config.USERPROF_TAG_HOBBYTAG)) {
                    Log.d(LogTag.USERPROFILE, "Got message: Hobby ");
                    ArrayList<String> hobbyTags = new ArrayList<String>();
                    hobbyTags = intent.getStringArrayListExtra(Config.USERPROF_TAG_HOBBYTAG);
                    int i = hobbyTags.size();
                    while (i > 0) {
                        i--;
                        mHobbyStr += (hobbyTags.get(i));
                        if(i>0)
                            mHobbyStr += ",";
                    }
                    EditText editText_hobby = (EditText) findViewById(R.id.textView_userprof_Hobbies);
                    editText_hobby.setText(mHobbyStr);

                } else if (messageSrc.equals(Config.USERPROF_TAG_FAVFOODTAG)) {
                    Log.d(LogTag.USERPROFILE, "Got message:FavFood ");
                    ArrayList<String> favfoodTags = new ArrayList<String>();
                    favfoodTags = intent.getStringArrayListExtra(Config.USERPROF_TAG_FAVFOODTAG);
                    int i = favfoodTags.size();

                    while (i > 0) {
                        i--;
                        mFavFoodStr += (favfoodTags.get(i));
                        if(i>0)
                            mFavFoodStr += ",";
                    }
                    EditText editText_favFood = (EditText) findViewById(R.id.textView_userprof_favouritefood);
                    editText_favFood.setText(mFavFoodStr);

                }else if(messageSrc.equals(Config.USERPROF_TAG_LOCATION)){
                    Log.d(LogTag.USERPROFILE, "Got message:Location ");
                   /* intent.getDoubleExtra(Config.CURR_LOCATION_LAT,mLatitude);
                    intent.getDoubleExtra(Config.CURR_LOCATION_LONG,mLongitude);*/
                    Bundle extras = intent.getExtras();
                    mLatitude = extras.getDouble(Config.CURR_LOCATION_LAT);
                    mLongitude = extras.getDouble(Config.CURR_LOCATION_LONG);
                    if((mLatitude== 0.0) || (mLongitude == 0.0)){
                        HelpUtils.showAlertMessageToUser(UserProfileActivity.this,getString(R.string.app_name),getString(R.string.txt_label_operation_failed));
                        return;
                    }
                    Geocoder geocoder;
                    List<Address> addresses = null;
                    geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        addresses = geocoder.getFromLocation(mLatitude, mLongitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();                }

                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    EditText userLocationET = (EditText) findViewById(R.id.editText_userprof_location);
                    mLocalAddress = city + ','+ postalCode;
                    userLocationET.setText(mLocalAddress);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter(Config.WK_LOCALBROADCAST_USERPROF));
    }

    private void handleUserProfileSave(){
        Log.d(LogTag.USERPROFILE, "UserProfile:handleUserProfileSave Start");
        //1.Get the user entered name
        EditText userNameET = (EditText) findViewById(R.id.et_userprofile_name);
        mUserName = userNameET.getText().toString();
        //Check if email field is empty
        /*if (TextUtils.isEmpty(mUserName)) {
            userNameET.setError(getResources().getString(R.string.alert_email));
            userNameET.requestFocus();
            return;
        }*/

        //3.Incase if user entered without choosing from googlemap
        EditText locationET = (EditText) findViewById(R.id.editText_userprof_location);
        mLocalAddress = locationET.getText().toString();

        //2.Gender & 4.Distance
        if(mUserName.equals("") ||mGender.equals("") || (mUserDistanceSelection == 0) || mLocalAddress.equals("")
                || mFavFoodStr.equals("") || mHobbyStr.equals("")){
            HelpUtils.showAlertMessageToUser(UserProfileActivity.this,
                    getString(R.string.app_name),getString(R.string.txt_fill_operation_failed));
            return;
        }

        sendDetailsToServer();
        Log.d(LogTag.USERPROFILE, "UserProfile:handleUserProfileSave End");
    }

    private TagContainerLayout mTagContainerLayout_hobby;

    private void showHobbyDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_user_profile_hobby);

        mTagContainerLayout_hobby = (TagContainerLayout) dialog.findViewById(R.id.tagcontainerLayout1);
        // Set custom click listener
        mTagContainerLayout_hobby.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                //Toast.makeText(UserProfileHobbyActivity.this, "click-position:" + position + ", text:" + text,
                //Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTagLongClick(final int position, String text) {
                if (position < mTagContainerLayout_hobby.getChildCount()) {
                    mTagContainerLayout_hobby.removeTag(position);
                }
            }
            @Override
            public void onTagCrossClick(int position) {
                mTagContainerLayout_hobby.removeTag(position);
            }
        });

        Button btnAddTag = (Button) dialog.findViewById(R.id.btn_add_tag);
        btnAddTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText text = (EditText) dialog.findViewById(R.id.text_tag);
                String toAddStr = text.getText().toString();
                List<String> tagList = mTagContainerLayout_hobby.getTags();
                if (tagList.contains(toAddStr)) {
                    HelpUtils.showAlertMessageToUser
                            (UserProfileActivity.this, getString(R.string.app_name),
                                    "Value exists already!");
                    text.setText("");
                    return;
                }
                mTagContainerLayout_hobby.addTag(text.getText().toString());
                text.setText("");
            }
        });

        Button btnDone = (Button) dialog.findViewById(R.id.btn_finish);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

}
