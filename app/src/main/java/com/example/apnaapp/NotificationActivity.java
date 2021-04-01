package com.example.apnaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class NotificationActivity extends AppCompatActivity {

    private RecyclerView notificationList;
    private DatabaseReference friendRequestRef,contactRef,userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        friendRequestRef= FirebaseDatabase.getInstance().getReference().child("Friends Request");
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();

        notificationList=findViewById(R.id.notification_list);
        notificationList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    public static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView userNmaeTxt;
        Button acceptn,cancelbtn;
        ImageView profileImage;
        RelativeLayout cardView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            userNmaeTxt=itemView.findViewById(R.id.name_nptification);
            acceptn=itemView.findViewById(R.id.request_accept_btn);
            cardView=itemView.findViewById(R.id.card);
            cancelbtn=itemView.findViewById(R.id.request_decline_btn);
            profileImage=itemView.findViewById(R.id.image_Notification);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(friendRequestRef.child(currentUserId),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,NotificationViewHolder> firebaseRecyclerAdapter=
                new FirebaseRecyclerAdapter<Contacts, NotificationViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final NotificationViewHolder holder, int i, @NonNull Contacts contacts) {
                      holder.acceptn.setVisibility(View.VISIBLE);
                      holder.cancelbtn.setVisibility(View.VISIBLE);

                 final String listUserId=getRef(i).getKey();
                      DatabaseReference requetTypeRef=getRef(i).child("request_type").getRef();
                      requetTypeRef.addValueEventListener(new ValueEventListener() {
                          @Override
                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                              if (dataSnapshot.exists()){
                                  String type=dataSnapshot.getValue().toString();
                                  if (type.equals("received"))
                                  {
                                      holder.cardView.setVisibility(View.VISIBLE);

                                      userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                                          @Override
                                          public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                              if (dataSnapshot.hasChild("image")) {
                                                  final String imageStr=dataSnapshot.child("image").getValue().toString();

                                                  Picasso.get().load(imageStr).into(holder.profileImage);
                                              }
                                              final String nameStr=dataSnapshot.child("name").getValue().toString();
                                              holder.userNmaeTxt.setText(nameStr);

                                              holder.acceptn.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {

                                                      contactRef.child(currentUserId).child(listUserId)
                                                              .child("Contacts").setValue("Saved")
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                      if (task.isSuccessful()){

                                                                          contactRef.child(listUserId).child(currentUserId)
                                                                                  .child("Contacts").setValue("Saved")
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          if (task.isSuccessful()){
                                                                                              friendRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                          @Override
                                                                                                          public void onComplete(@NonNull Task<Void> task) {
                                                                                                              if (task.isSuccessful()){
                                                                                                                  friendRequestRef.child(listUserId).child(currentUserId).removeValue()
                                                                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                              @Override
                                                                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                  if (task.isSuccessful()){
                                                                                                                                      Toast.makeText(NotificationActivity.this, "Contact Saved", Toast.LENGTH_SHORT).show();
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
                                              });

                                              holder.cancelbtn.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {

                                                      friendRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                      if (task.isSuccessful()){
                                                                          friendRequestRef.child(currentUserId).child(listUserId).removeValue()
                                                                                  .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          if (task.isSuccessful()){
                                                                                              Toast.makeText(NotificationActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                                                                                          }
                                                                                      }
                                                                                  });
                                                                      }
                                                                  }
                                                              });

                                                  }
                                              });
                                          }

                                          @Override
                                          public void onCancelled(@NonNull DatabaseError databaseError) {

                                          }
                                      });


                                  }else {
                                      holder.cardView.setVisibility(View.GONE);
                                  }
                              }
                          }

                          @Override
                          public void onCancelled(@NonNull DatabaseError databaseError) {

                          }
                      });
                    }

                    @NonNull
                    @Override
                    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.find_friend_design,parent,false);
                        NotificationViewHolder viewHolder=new NotificationViewHolder(view);
                        return viewHolder;
                    }
                };
        notificationList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

}
