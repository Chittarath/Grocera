package com.theindiecorp.grocera;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Data.User;

import java.util.concurrent.TimeUnit;

public class ManageProfileActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    EditText nameEt,phoneNumberEt,emailEt,pinCodeEt,otpEt;
    TextView saveBtn,verifyOtp;
    String codeSent,phoneTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_profile);
        nameEt = findViewById(R.id.name);
        phoneNumberEt = findViewById(R.id.phone_number);
        emailEt = findViewById(R.id.email);
        pinCodeEt = findViewById(R.id.pin_code);
        otpEt = findViewById(R.id.otp);
        verifyOtp = findViewById(R.id.verify_otp);

        databaseReference.child("users").child(auth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                nameEt.setText(user.getName());
                phoneNumberEt.setText(user.getPhone());
                emailEt.setText(user.getEmail());
                pinCodeEt.setText(user.getPinCode());
                phoneTxt = user.getPhone();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        saveBtn = findViewById(R.id.update);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(TextUtils.isEmpty(nameEt.getText())){
                    nameEt.setError("Enter name");
                    return;
                }
                if(TextUtils.isEmpty(emailEt.getText())){
                    emailEt.setError("Enter email");
                    return;
                }
                if(TextUtils.isEmpty(phoneNumberEt.getText())){
                    phoneNumberEt.setError("Enter phone number");
                    return;
                }
                if(TextUtils.isEmpty(pinCodeEt.getText())){
                    pinCodeEt.setError("Enter PIN code");
                    return;
                }

                User user = new User();
                user.setId(auth.getCurrentUser().getUid());
                user.setPhone("+91" + phoneNumberEt.getText().toString());
                user.setName(nameEt.getText().toString());
                user.setPinCode(pinCodeEt.getText().toString());

                if(phoneTxt.equals(user.getPhone())){
                    databaseReference.child("users").child(auth.getCurrentUser().getUid()).setValue(user);
                    Toast.makeText(ManageProfileActivity.this,"Profile Updated",Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ManageProfileActivity.this,MainActivity.class));
                    finish();
                }
                else{
                    if(!phoneNumberEt.getText().toString().startsWith("+91"))
                        phoneNumberEt.setText(String.format("+91%s",phoneNumberEt.getText().toString()));
                    sendVerificationCode();
                }

            }
        });

        verifyOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
            }
        });

        Button goBackBtn = findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageProfileActivity.this,MainActivity.class));
            }
        });

    }
    private void verifySignInCode() {
        String code = otpEt.getText().toString();
        if (code.length() != 6) {
            otpEt.setError("Enter a valid OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        FirebaseAuth.getInstance().getCurrentUser().updatePhoneNumber(credential);

        User user = new User();
        user.setEmail(emailEt.getText().toString());
        user.setName(nameEt.getText().toString());
        user.setId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        user.setPhone(phoneNumberEt.getText().toString());
        user.setPinCode(pinCodeEt.getText().toString());

        databaseReference.child("users").child(auth.getCurrentUser().getUid()).setValue(user);

        Toast.makeText(this,"Updated",Toast.LENGTH_SHORT).show();
        startActivity(new Intent(ManageProfileActivity.this,MainActivity.class));
        finish();
    }

    private void sendVerificationCode() {

        String phone = phoneNumberEt.getText().toString();

        if (phone.isEmpty()) {
            phoneNumberEt.setError("Phone no. is required");
            phoneNumberEt.requestFocus();
            otpEt.setVisibility(View.INVISIBLE);
            return;
        }

        if (phone.length() < 10) {
            phoneNumberEt.setError("Enter valid phone number");
            phoneNumberEt.requestFocus();
            otpEt.setVisibility(View.INVISIBLE);
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                ManageProfileActivity.this,
                mCallbacks
        );

        Log.e("code", "sent");

        phoneNumberEt.setVisibility(View.VISIBLE);
        saveBtn.setVisibility(View.GONE);
        verifyOtp.setVisibility(View.VISIBLE);
        otpEt.setVisibility(View.VISIBLE);

    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

        }

        @Override
        public void onVerificationFailed(FirebaseException e) {

        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };

}
