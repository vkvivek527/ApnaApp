package com.example.apnaapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class Main2Activity extends AppCompatActivity {
    private BottomNavigationView navView;
    private RecyclerView myContactList;
    private ImageView findPeopleBtn;
    private DatabaseReference contactRef,userRef;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private String username="",profilestrImage="";
    private String calledBy="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        navView = findViewById(R.id.nav_view);

        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        userRef= FirebaseDatabase.getInstance().getReference().child("Users");


        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        findPeopleBtn=findViewById(R.id.findPeople);
        myContactList=findViewById(R.id.contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Main2Activity.this,FindPeopleActivity.class);
                startActivity(intent);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener=new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()){
                case R.id.navigation_home:
                    Intent mainIntent=new Intent(Main2Activity.this,Main2Activity.class);
                    startActivity(mainIntent);
                    break;
                case R.id.navigation_setting:
                    Intent settingIntent=new Intent(Main2Activity.this,SettingActivity.class);
                    startActivity(settingIntent);
                    break;
                case R.id.navigation_notifications:
                    Intent notificationIntent=new Intent(Main2Activity.this,NotificationActivity.class);
                    startActivity(notificationIntent);
                    break;
                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent=new Intent(Main2Activity.this,MainActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }
            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        validateuser();

        checkForReceivingCall();

        FirebaseRecyclerOptions<Contacts> options=
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef.child(currentUserId),Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> firebaseRecyclerAdapter=
               new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
                   @Override
                   protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int i, @NonNull Contacts contacts) {

                       final  String listUserId=getRef(i).getKey();

                       userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                           @Override
                           public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                               if (dataSnapshot.exists()){
                                username=dataSnapshot.child("name").getValue().toString();
                                profilestrImage=dataSnapshot.child("image").getValue().toString();

                                holder.userNmaeTxt.setText(username);
                                   Picasso.get().load(profilestrImage).into(holder.profileImage);

                               }
                               holder.callBtn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       Intent callingIntent=new Intent(Main2Activity.this,CallingActivity.class);
                                      callingIntent.putExtra("visit_user_id",listUserId);
                                       startActivity(callingIntent);

                                   }
                               });
                           }

                           @Override
                           public void onCancelled(@NonNull DatabaseError databaseError) {

                           }
                       });
                   }

                   @NonNull
                   @Override
                   public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                       ContactsViewHolder viewHolder=new ContactsViewHolder(view);
                       return viewHolder;
                   }
               };
        myContactList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private void checkForReceivingCall() {

        userRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    if (dataSnapshot.hasChild("ringing")){
                        calledBy=dataSnapshot.child("ringing").getValue().toString();
                        Intent callingIntent=new Intent(Main2Activity.this,CallingActivity.class);
                        callingIntent.putExtra("visit_user_id",calledBy);
                        startActivity(callingIntent);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private  void validateuser(){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();

        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()){
                    Intent setting=new Intent(Main2Activity.this,SettingActivity.class);
                    startActivity(setting);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userNmaeTxt;
        Button callBtn;
        ImageView profileImage;


        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userNmaeTxt=itemView.findViewById(R.id.name_contact);
            callBtn=itemView.findViewById(R.id.call_video);
            profileImage=itemView.findViewById(R.id.image_contact);

        }
    }

}
