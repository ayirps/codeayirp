package com.priya.ck.weekuk;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;
import helper.Config;
import helper.HelpUtils;
import helper.LogTag;

public class UserProfileFavFoodActivity extends AppCompatActivity implements View.OnClickListener{
    private TagContainerLayout mTagContainerLayout1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userprofile_favfood);

        TextView labelTxt = findViewById(R.id.userProf_tagtitle_food);
        labelTxt.setText(getString(R.string.txt_label_favfood));

        mTagContainerLayout1 = (TagContainerLayout) findViewById(R.id.tagcontainerLayout1_food);
        // Set custom click listener
        mTagContainerLayout1.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                //Toast.makeText(UserProfileHobbyActivity.this, "click-position:" + position + ", text:" + text,
                //Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTagLongClick(final int position, String text) {
                AlertDialog dialog = new AlertDialog.Builder(UserProfileFavFoodActivity.this)
                        .setTitle("long click")
                        .setMessage("You will delete this tag!")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (position < mTagContainerLayout1.getChildCount()) {
                                    mTagContainerLayout1.removeTag(position);
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
            }

            @Override
            public void onTagCrossClick(int position) {
                mTagContainerLayout1.removeTag(position);
            }
        });

        Button btnAddTag = (Button) findViewById(R.id.btn_add_tagfood);
        btnAddTag.setOnClickListener(this);

        Button btnDone = (Button) findViewById(R.id.btn_finishfood);
        btnDone.setOnClickListener(this);
    }

    public void onClick(View view) {
        Log.d(LogTag.USERPROFILE, "UserProfile:OnClick Start");
        switch(view.getId()) {
            case R.id.btn_finishfood:{
                /*Intent returnIntent = new Intent(UserProfileFavFoodActivity.this, UserProfileActivity.class);
                returnIntent.putExtra(Config.USERPROF_TAG_FAVFOODTAG, (ArrayList<String>) (mTagContainerLayout1.getTags()));
                setResult(RESULT_OK,returnIntent);
                startActivity(returnIntent);*/
                sendMessage();
                finish();
                break;
            }
            case R.id.btn_add_tagfood:{
                final EditText text = (EditText) findViewById(R.id.text_tagfood);
                String toAddStr = text.getText().toString();
                List<String> tagList = mTagContainerLayout1.getTags();
                if (tagList.contains(toAddStr)) {
                    HelpUtils.showAlertMessageToUser
                            (UserProfileFavFoodActivity.this, getString(R.string.app_name),
                                    "Value exists already!");
                    text.setText("");
                    return;
                }
                mTagContainerLayout1.addTag(text.getText().toString());
                text.setText("");
                break;
            }
        }
    }

    // Send an Intent with an action named "custom-event-name". The Intent sent should
// be received by the ReceiverActivity.
    private void sendMessage() {
        Log.d(LogTag.USERPROFILE, "Broadcasting message - Favourite Foods");
        Intent intent = new Intent(Config.WK_LOCALBROADCAST_USERPROF);
        // You can also include some extra data.
        intent.putExtra("Source",Config.USERPROF_TAG_FAVFOODTAG);
        intent.putExtra(Config.USERPROF_TAG_FAVFOODTAG, (ArrayList<String>) (mTagContainerLayout1.getTags()));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
