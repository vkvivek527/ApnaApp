package com.example.apnaapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {
    private Button saveBtn;
    private EditText usernameEt,userBioEt;
    private ImageView prifileImageView;
    private static  int gallery_Pick=1;
    private Uri imageUri;
    private StorageReference userProfileRef;
    private String downLoadUrl;
    private DatabaseReference userRef;
    private ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        saveBtn=findViewById(R.id.save_setting_btn);
        userBioEt=findViewById(R.id.userstatus_setting);
        usernameEt=findViewById(R.id.username_setting);
        prifileImageView=findViewById(R.id.setting_profile_image);
        dialog=new ProgressDialog(this);

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        userProfileRef= FirebaseStorage.getInstance().getReference().child("Profile Images");
        prifileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,gallery_Pick);
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserData();
            }
        });
        retrieveUserInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==gallery_Pick &&resultCode==RESULT_OK && data!=null ){
            imageUri=data.getData();
            prifileImageView.setImageURI(imageUri);

        }
    }

    private void saveUserData() {
        final String userName=usernameEt.getText().toString();
        final String userStatus=userBioEt.getText().toString();

        if (imageUri==null){
                 userRef.addValueEventListener(new ValueEventListener() {
                     @Override
                     public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                         if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                             saveInfoWithoutImage();
                         }else {
                             Toast.makeText(SettingActivity.this, "Please select image", Toast.LENGTH_SHORT).show();
                         }

                     }

                     @Override
                     public void onCancelled(@NonNull DatabaseError databaseError) {

                     }
                 });

        }else  if (userName.equals("")){
            Toast.makeText(this, "Plesae Enter Name", Toast.LENGTH_SHORT).show();
        }else if (userStatus.equals("")){
            Toast.makeText(this, "Enter Status", Toast.LENGTH_SHORT).show();
        }else {
            dialog.setTitle("Updating");
            dialog.setMessage("Please wait...");
            dialog.show();
            final StorageReference filePath=userProfileRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            final UploadTask uploadTask=filePath.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (task.isSuccessful()){

                        downLoadUrl=filePath.getDownloadUrl().toString();
                    }else {
                        Toast.makeText(SettingActivity.this, "Error "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downLoadUrl=task.getResult().toString();
                        HashMap<String,Object> profileMap=new HashMap<>();
                        profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name",userName);
                        profileMap.put("status",userStatus);
                        profileMap.put("image",downLoadUrl);

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                  if (task.isSuccessful()){
                                      Intent intent=new Intent(SettingActivity.this,Main2Activity.class);
                                       startActivity(intent);
                                       finish();
                                      Toast.makeText(SettingActivity.this, "Updated Sucessfully", Toast.LENGTH_SHORT).show();
                                      dialog.dismiss();
                                  }else {
                                      dialog.dismiss();
                                      Toast.makeText(SettingActivity.this, "Error:-"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                  }

                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfoWithoutImage() {

        final String userName=usernameEt.getText().toString();
        final String userStatus=userBioEt.getText().toString();



        if (userName.equals("")){
            Toast.makeText(this, "Plesae Enter Name", Toast.LENGTH_SHORT).show();
        }else if (userStatus.equals("")){
            Toast.makeText(this, "Enter Status", Toast.LENGTH_SHORT).show();
        }else {

            dialog.setTitle("Updating");
            dialog.setMessage("Please wait...");
            dialog.show();

            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("uid",FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name",userName);
            profileMap.put("status",userStatus);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Intent intent=new Intent(SettingActivity.this,Main2Activity.class);
                        startActivity(intent);
                        finish();
                        dialog.dismiss();
                        Toast.makeText(SettingActivity.this, "Updated Sucessfully", Toast.LENGTH_SHORT).show();
                    }else {
                        dialog.dismiss();
                        Toast.makeText(SettingActivity.this, "Error:-"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }


    }

    private void retrieveUserInfo(){
        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.exists()){
                            String imageFromDb=dataSnapshot.child("image").getValue().toString();
                            String nameFromDb=dataSnapshot.child("name").getValue().toString();
                            String statusFromDb=dataSnapshot.child("status").getValue().toString();

                            usernameEt.setText(nameFromDb);
                            userBioEt.setText(statusFromDb);
                            Picasso.get().load(imageFromDb).placeholder(R.drawable.profile_image).into(prifileImageView);
                         }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

}
