package com.example.apnaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {
    private RecyclerView findFriendList;
    private EditText searchEt;
    private String str="";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        searchEt=findViewById(R.id.search_user_text);
        findFriendList=findViewById(R.id.find_friends_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

       searchEt.addTextChangedListener(new TextWatcher() {
           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {

           }

           @Override
           public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

               if (searchEt.getText().toString().equals("")){
                   Toast.makeText(FindPeopleActivity.this, "Nothing to search", Toast.LENGTH_SHORT).show();
               }else {
                     str=charSequence.toString();
                     onStart();
               }
           }

           @Override
           public void afterTextChanged(Editable s) {

           }
       });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options=null;
        if (str.equals("")){
            options=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef,Contacts.class)
                    .build();
        }else {
            options=new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef.orderByChild("name")
                                    .startAt(str)
                            .endAt(str+"\uf8ff"),
                            Contacts.class)
                             .build();
        }
        FirebaseRecyclerAdapter<Contacts,FindPeopleViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Contacts, FindPeopleViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindPeopleViewHolder holder, final int i, @NonNull final Contacts contacts) {
              holder.userNmaeTxt.setText(contacts.getName());
                Picasso.get().load(contacts.getImage()).into(holder.profileImage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id=getRef(i).getKey();
                        Intent intent=new Intent(FindPeopleActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_id);
                        intent.putExtra("profile_image",contacts.getImage());
                        intent.putExtra("profile_name",contacts.getName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindPeopleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                FindPeopleViewHolder viewHolder=new FindPeopleViewHolder(view);
                return viewHolder;
            }
        };
       findFriendList.setAdapter(firebaseRecyclerAdapter);
       firebaseRecyclerAdapter.startListening();
    }

    public static class FindPeopleViewHolder extends RecyclerView.ViewHolder {
        TextView userNmaeTxt;
        Button videoCallbtn;
        ImageView profileImage;
        RelativeLayout card;

        public FindPeopleViewHolder(@NonNull View itemView) {
            super(itemView);
            userNmaeTxt=itemView.findViewById(R.id.name_contact);
            videoCallbtn=itemView.findViewById(R.id.call_video);
            card=itemView.findViewById(R.id.card_view);
            profileImage=itemView.findViewById(R.id.image_contact);

            videoCallbtn.setVisibility(View.GONE);
        }
    }

}
