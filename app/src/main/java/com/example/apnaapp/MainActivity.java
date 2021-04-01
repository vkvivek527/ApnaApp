package com.example.apnaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private CountryCodePicker ccp;
    private EditText phoneText;
    private EditText codeText;
    private Button continueBtn;
    private String checker="",phoneNumber="";
    private RelativeLayout relativeLayout;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);

        phoneText=findViewById(R.id.phoneText);
        codeText=findViewById(R.id.codeText);
        continueBtn=findViewById(R.id.continueNextButton);
        relativeLayout=findViewById(R.id.phoneAuth);
        ccp=(CountryCodePicker)findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(phoneText);

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (continueBtn.getText().equals("submit")|| checker.equals("code sent")){
                    String verificationCode=codeText.getText().toString();
                    if (verificationCode.equals("")){
                        Toast.makeText(MainActivity.this, "Enter Code", Toast.LENGTH_SHORT).show();
                    }else {
                        loadingBar.setTitle("Verifying Code");
                        loadingBar.setMessage("Please Wait");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential=PhoneAuthProvider.getCredential(mVerificationId,verificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }else {
                    phoneNumber=ccp.getFullNumberWithPlus();
                    if (!phoneNumber.equals("")){
                        loadingBar.setTitle("Verifying Phone Number");
                        loadingBar.setMessage("Please Wait");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                       PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber,60,TimeUnit.SECONDS,MainActivity.this,mCallbacks);

                    }else {
                        Toast.makeText(MainActivity.this, "Enter Valid Phone Number", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
          mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
              @Override
              public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
               signInWithPhoneAuthCredential(phoneAuthCredential);
              }

              @Override
              public void onVerificationFailed(@NonNull FirebaseException e) {
                  loadingBar.dismiss();
                  Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                  relativeLayout.setVisibility(View.VISIBLE);
                  continueBtn.setText("Continue");
                  codeText.setVisibility(View.GONE);

              }
              @Override
              public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                  super.onCodeSent(s, forceResendingToken);
                  mVerificationId=s;
                  mResendToken=forceResendingToken;
                  loadingBar.dismiss();
                  relativeLayout.setVisibility(View.GONE);
                  checker="code sent";
                  continueBtn.setText("Submit");
                  codeText.setVisibility(View.VISIBLE);
                  Toast.makeText(MainActivity.this, "Code Sent", Toast.LENGTH_SHORT).show();

              }
          };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                       loadingBar.dismiss();
                            Toast.makeText(MainActivity.this, "Signed In Sucessfully", Toast.LENGTH_SHORT).show();
                            Intent intent=new Intent(MainActivity.this,Main2Activity.class);
                            startActivity(intent);
                            finish();
                        } else {
                       loadingBar.dismiss();
                       String error=task.getException().toString();
                            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser firebaseUser=FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser!=null){
            Intent intent=new Intent(MainActivity.this,Main2Activity.class);
            startActivity(intent);
            finish();
        }
    }
}
