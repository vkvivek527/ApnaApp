package com.example.apnaapp;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener,Publisher.PublisherListener {

    private static String API_Key="46730702";
    private static String SESSION_ID="1_MX40NjczMDcwMn5-MTU4OTE5MzEwMjg3Nn5odlNTK0RNdjFHSlZHcVVMZCtHdXBuVjh-fg";
    private static String TOKEN="T1==cGFydG5lcl9pZD00NjczMDcwMiZzaWc9MWNjODI5NzkxMDcxODk4Mjk3YTdmYTg3MDE3MDRjZTc2OTI5OTA5NzpzZXNzaW9uX2lkPTFfTVg0ME5qY3pNRGN3TW41LU1UVTRPVEU1TXpFd01qZzNObjVvZGxOVEswUk5kakZIU2xaSGNWVk1aQ3RIZFhCdVZqaC1mZyZjcmVhdGVfdGltZT0xNTg5MTkzMjI2Jm5vbmNlPTAuODU5MjY1NzUxNzUwMzQ5NSZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkxNzg1MjI1JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG=VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERMISSION =124;
    private DatabaseReference usersRef;
    private String userID="";
    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;
    private ImageView closeVideoChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        closeVideoChatBtn=findViewById(R.id.close_video_chat_btn);
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.child(userID).hasChild("Ringing")){
                            usersRef.child(userID).child("Ringing").removeValue();
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                             finish();
                        }
                        if (dataSnapshot.child(userID).hasChild("Calling")){
                            usersRef.child(userID).child("Calling").removeValue();
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }else {
                            if (mPublisher!=null){
                                mPublisher.destroy();
                            }
                            if (mSubscriber!=null){
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this,MainActivity.class));
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });
        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,VideoChatActivity.this);
    }
         @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private void requestPermissions(){
      String[] perms={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};

      if (EasyPermissions.hasPermissions(this,perms)){

          mPublisherViewController=findViewById(R.id.publisher_container);
          mSubscriberViewController=findViewById(R.id.subscriber_container);

          //intilise and connect to session
         mSession=new Session.Builder(this,API_Key,SESSION_ID).build();
         mSession.setSessionListener(VideoChatActivity.this);
         mSession.connect(TOKEN);
      }
      else {
          EasyPermissions.requestPermissions(this,"Allow Permission for video call",RC_VIDEO_APP_PERMISSION,perms);
      }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

   //2.publishing a stream to the session
    @Override
    public void onConnected(Session session)
    {
        Toast.makeText(this, "connected", Toast.LENGTH_SHORT).show();
        mPublisher=new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if (mPublisher.getView() instanceof GLSurfaceView){

            ((GLSurfaceView)mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show();

    }

    ///3.subscribing to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Received");

        if (mSubscriber==null){
            mSubscriber=new Subscriber.Builder(this,stream).build();
             mSession.subscribe(mSubscriber);
             mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Droped");
        if (mSubscriber!=null){
            mSubscriber=null;
            mSubscriberViewController.removeAllViews();
        }
        Toast.makeText(this, "dropped", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
