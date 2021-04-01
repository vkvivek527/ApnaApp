package com.example.apnaapp;


import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {
    private TextView nameContact;
    private ImageView profileImage;
    private ImageView acceptCallBtn,cancelCallBtn;
    private DatabaseReference userRef;
    private String checker="";
    private String receiverUserId="",receiverUserImage="",receiverUserName="";
    private String senderUserId="",senderUserImage="",senderUserName="";
    private String callingID="",ringingID;

    private MediaPlayer mediaPlayer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        mediaPlayer=MediaPlayer.create(this,R.raw.ringing);

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");

          senderUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();

        nameContact=findViewById(R.id.name_calling);
        profileImage=findViewById(R.id.profile_image_calling);
        acceptCallBtn=findViewById(R.id.video_call_btn);
        cancelCallBtn=findViewById(R.id.cancel_call_btn);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
              checker="clicked";
              cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                final HashMap<String,Object> callingPickupMap=new HashMap<>();
                callingPickupMap.put("picked","picked");

                userRef.child(senderUserId).child("Ringing").updateChildren(callingPickupMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                   Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                                   startActivity(intent);
                                }
                            }
                        });
            }
        });

        getAndSetUserProfileInfo();
    }

    private void cancelCallingUser() {

       /////from sender side
        userRef.child(senderUserId).child("Calling")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("calling")) {
                            callingID = dataSnapshot.child("calling").getValue().toString();

                            userRef.child(callingID).child("Ringing")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        userRef.child(senderUserId)
                                                .child("Calling").removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }

                                }
                            });
                        } else {
                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        //////receiver side

        userRef.child(senderUserId).child("Ringing")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                        if (dataSnapshot.exists() && dataSnapshot.hasChild("ringing")) {
                            ringingID = dataSnapshot.child("ringing").getValue().toString();

                            userRef.child(ringingID).child("Calling")
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        userRef.child(senderUserId)
                                                .child("Ringing").removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }

                                }
                            });
                        } else {
                            startActivity(new Intent(CallingActivity.this, MainActivity.class));
                            finish();
                        }
                    }
                }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    private void getAndSetUserProfileInfo() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if ( dataSnapshot.child(receiverUserId).exists()){
                    receiverUserImage=dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUserName=dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUserName);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
                if (dataSnapshot.child(senderUserId).exists()){
                    senderUserImage=dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUserName=dataSnapshot.child(senderUserId).child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();
        userRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing")){
                    final HashMap<String,Object> callingInfo=new HashMap<>();
//                    callingInfo.put("uid",senderUserId);
//                    callingInfo.put("name",senderUserName);
//                    callingInfo.put("image",senderUserImage);
                    callingInfo.put("calling",receiverUserId);

                    userRef.child(senderUserId).child("Calling")
                            .updateChildren(callingInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                             if (task.isSuccessful()){
                                 final HashMap<String,Object> ringingInfo=new HashMap<>();

                                 ringingInfo.put("ringing",senderUserId);

                                 userRef.child(receiverUserId).child("Ringing")
                                         .updateChildren(ringingInfo);
                             }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling")){

                    acceptCallBtn.setVisibility(View.VISIBLE);
                }
                if (dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked")){
                    mediaPlayer.stop();
                    Intent intent=new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
