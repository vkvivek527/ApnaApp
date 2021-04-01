package com.example.apnaapp;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {
    private String receiverUserId="",receiverUserName="",receiverUserImage="";

    private ImageView backGroundProfile;
    private TextView nameProfile;
    private Button addFriend,cancelFriendRequest;
    private FirebaseAuth mAuth;
    private String senderUserId;
    private String currentState="new";
    private DatabaseReference friendRequestRef,contactRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();
        senderUserId=mAuth.getCurrentUser().getUid();

        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friends Request");
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserId=getIntent().getExtras().get("visit_user_id").toString();
        receiverUserImage=getIntent().getExtras().get("profile_image").toString();
        receiverUserName=getIntent().getExtras().get("profile_name").toString();

        backGroundProfile=findViewById(R.id.background_profile_view);
        nameProfile=findViewById(R.id.name_profile);
        addFriend=findViewById(R.id.add_friend);
        cancelFriendRequest=findViewById(R.id.decline_friend_request);

        Picasso.get().load(receiverUserImage).into(backGroundProfile);
        nameProfile.setText(receiverUserName);

        manageClickEvent();
    }

    private void manageClickEvent() {
        friendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserId)){
                            String requestType=dataSnapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (requestType.equals("sent")){
                                currentState="request_sent";
                                addFriend.setText("Cancel Request");

                            }else if (requestType.equals("received")){
                                currentState="request_received";
                                addFriend.setText("Accept Request");

                                cancelFriendRequest.setVisibility(View.VISIBLE);

                                cancelFriendRequest.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelRequest();
                                    }
                                });
                            }
                        }else {
                               contactRef.child(senderUserId)
                                       .addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                               if (dataSnapshot.hasChild(receiverUserId)){
                                                   currentState="friends";
                                                   addFriend.setText("Delete Contact");
                                               }else {
                                                   currentState="new";
                                               }
                                           }

                                           @Override
                                           public void onCancelled(@NonNull DatabaseError databaseError) {

                                           }
                                       });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (senderUserId.equals(receiverUserId)){
            addFriend.setVisibility(View.GONE);
        }else {
            addFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentState.equals("new")){

                        sendFriendRequest();

                    }
                    if (currentState.equals("request_sent")){
                        cancelRequest();
                    }
                    if (currentState.equals("request_received")){
                     aaceptFriendRequest();
                    }
                    if (currentState.equals("request_sent")){
                         cancelRequest();
                    }
                }
            });
        }

    }

    private void aaceptFriendRequest() {
        contactRef.child(senderUserId).child(receiverUserId)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                contactRef.child(receiverUserId).child(senderUserId)
                                        .child("Contacts").setValue("Saved")
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                               if (task.isSuccessful()){
                                                   friendRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if (task.isSuccessful()){
                                                                       friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                                                               .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                   @Override
                                                                                   public void onComplete(@NonNull Task<Void> task) {
                                                                                       if (task.isSuccessful()){
                                                                                           currentState="friends";
                                                                                           addFriend.setText("Delete Contact");
                                                                                           cancelFriendRequest.setVisibility(View.GONE);
                                                                                       }
                                                                                   }
                                                                               });
                                                                   }
                                                               }
                                                           });
                                               }
                                            }
                                        });
                            }
                    }
                });
    }

    private void cancelRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            friendRequestRef.child(receiverUserId).child(senderUserId).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                currentState="new";
                                                addFriend.setText("Add Friend");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void sendFriendRequest() {
        friendRequestRef.child(senderUserId).child(receiverUserId).
                child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                      if (task.isSuccessful()){
                          friendRequestRef.child(receiverUserId).child(senderUserId).
                                  child("request_type").setValue("received").addOnCompleteListener(new OnCompleteListener<Void>() {
                              @Override
                              public void onComplete(@NonNull Task<Void> task) {
                                      if (task.isSuccessful()){
                                          currentState="request_sent";
                                          addFriend.setText("Cancel Request");
                                          Toast.makeText(ProfileActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                                      }
                              }
                          });
                      }
                    }
                });


    }
}
