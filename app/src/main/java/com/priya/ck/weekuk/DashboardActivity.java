package com.priya.ck.weekuk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import helper.AllMethods;

public class DashboardActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "DashboardActivity";
    FirebaseAuth auth;
    FirebaseDatabase database;
    DatabaseReference messagedb;
    MessageAdapter adapter;
    User u;
    List<MessageData> messages;

    RecyclerView rvMessage;
    EditText etMessage;
    ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        u=new User();

        rvMessage=findViewById(R.id.rvMessage);
        etMessage=findViewById(R.id.etMessage);
        btnSend=findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        messages=new ArrayList<>();
    }

    @Override
    protected void onStart() {
        super.onStart();
        final FirebaseUser currentUser=auth.getCurrentUser();
        u.setUid(currentUser.getUid());
        u.setEmail(currentUser.getEmail());
        database.getReference("user").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                u=dataSnapshot.getValue(User.class);
                u.setUid(currentUser.getUid());
                AllMethods.name=u.getName();
                Log.e(TAG, "onDataChange: "+AllMethods.name );
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messagedb=database.getReference("messages");
        messagedb.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                MessageData message=dataSnapshot.getValue(MessageData.class);
                message.setKey(dataSnapshot.getKey());
                messages.add(message);
                displayMessages(messages);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                MessageData message=dataSnapshot.getValue(MessageData.class);
                message.setKey(dataSnapshot.getKey());
//                Toast.makeText(DashboardActivity.this, "MessageData changed...", Toast.LENGTH_SHORT).show();
                List<MessageData> newMessages=new ArrayList<>();
                for (MessageData m:messages){
                    if(m.getKey().equals(message.getKey())){
                        newMessages.add(message);
                    }
                    else{
                        newMessages.add(m);
                    }
                }
                messages=newMessages;
                displayMessages(messages);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                MessageData message=dataSnapshot.getValue(MessageData.class);
                message.setKey(dataSnapshot.getKey());
//                Toast.makeText(DashboardActivity.this, "MessageData deleted...", Toast.LENGTH_SHORT).show();
                List<MessageData> newMessages=new ArrayList<>();
                for (MessageData m:messages){
                    if(!m.getKey().equals(message.getKey())){
                        newMessages.add(m);
                    }
                }
                messages=newMessages;
                displayMessages(messages);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void displayMessages(List<MessageData> messages) {
        rvMessage.setLayoutManager(new LinearLayoutManager(DashboardActivity.this));
        adapter=new MessageAdapter(DashboardActivity.this,messages,messagedb);
        rvMessage.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.menuLogout){
            auth.signOut();
            startActivity(new Intent(DashboardActivity.this,MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if(!TextUtils.isEmpty(etMessage.getText().toString())) {
            MessageData message = new MessageData(etMessage.getText().toString(), u.getName());
            etMessage.setText("");
            messagedb.push().setValue(message);
        }
        else {
            Toast.makeText(this, "Please write message.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        messages=new ArrayList<>();
    }
}
