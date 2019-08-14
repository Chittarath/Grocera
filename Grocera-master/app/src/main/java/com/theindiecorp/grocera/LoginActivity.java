package com.theindiecorp.grocera;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Data.User;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();


    private EditText inputPhone, firstCharOTP, secondCharOTP, thirdCharOTP, fourthCharOTP, fifthCharOTP, sixthCharOTP;
    LinearLayout inputCode; //Input Code is made of 4 EditTextView contained inside a LinearLayout
    TextView verifyPhoneBtn, verifyOTP;
    String codeSent;
    ImageView userImg;

    public static String userId;

    public static boolean userHasACar = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() != null){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
        }

        inputPhone = findViewById(R.id.login_phone);
        inputCode = findViewById(R.id.input_code);
        verifyPhoneBtn = findViewById(R.id.send_otp);
        verifyOTP = findViewById(R.id.verify_otp);
        firstCharOTP = findViewById(R.id.first_char_otp);
        secondCharOTP = findViewById(R.id.second_char_otp);
        thirdCharOTP = findViewById(R.id.third_char_otp);
        fourthCharOTP = findViewById(R.id.fourth_char_otp);
        fifthCharOTP = findViewById(R.id.fifth_char_otp);
        sixthCharOTP = findViewById(R.id.sixth_char_otp);

        firstCharOTP.addTextChangedListener(new OTPTextWatcher(firstCharOTP));
        secondCharOTP.addTextChangedListener(new OTPTextWatcher(secondCharOTP));
        thirdCharOTP.addTextChangedListener(new OTPTextWatcher(thirdCharOTP));
        fourthCharOTP.addTextChangedListener(new OTPTextWatcher(fourthCharOTP));
        fifthCharOTP.addTextChangedListener(new OTPTextWatcher(fifthCharOTP));
        sixthCharOTP.addTextChangedListener(new OTPTextWatcher(sixthCharOTP));

        //Validate Phone number and send OTP
        verifyPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputCode.setVisibility(View.VISIBLE);
                if (!inputPhone.getText().toString().startsWith("+91"))
                    inputPhone.setText(String.format("+91%s", inputPhone.getText().toString()));
                sendVerificationCode();
                closeKeyboard();
            }
        });
        //OTP sent

        verifyOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verifySignInCode();
            }
        });

    }


    private void closeKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void verifySignInCode() {
        String code = getInputCode();
        if (code.length() != 6) {
            sixthCharOTP.setError("Enter a valid OTP");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        final String phone = inputPhone.getText().toString();
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            userId = auth.getCurrentUser().getUid();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference ref = database.getReference("users");

                            ref.child(userId).child("id").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String uid = dataSnapshot.getValue(String.class);

                                    Query query = FirebaseDatabase.getInstance().getReference("users")
                                            .orderByChild("phone").equalTo(phone);
                                    query.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if(!dataSnapshot.exists()){
                                                writeNewUser(new User("", phone, userId));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                            startActivity(new Intent(LoginActivity.this,MainActivity.class));

                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(getApplicationContext(), "Invalid code", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void writeNewUser(User user) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(user.getId()).setValue(user);
    }

    private void sendVerificationCode() {

        String phone = inputPhone.getText().toString();

        if (phone.isEmpty()) {
            inputPhone.setError("Phone no. is required");
            inputPhone.requestFocus();
            inputCode.setVisibility(View.INVISIBLE);
            return;
        }

        if (phone.length() < 10) {
            inputPhone.setError("Enter valid phone number");
            inputPhone.requestFocus();
            inputCode.setVisibility(View.INVISIBLE);
            return;
        }

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,
                60,
                TimeUnit.SECONDS,
                LoginActivity.this,
                mCallbacks
        );

        Log.e("code", "sent");

        inputCode.setVisibility(View.VISIBLE);
        verifyPhoneBtn.setVisibility(View.GONE);
        verifyOTP.setVisibility(View.VISIBLE);

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

    private String getInputCode() {
        String firstChar = firstCharOTP.getText().toString();
        String secondChar = secondCharOTP.getText().toString();
        String thirdChar = thirdCharOTP.getText().toString();
        String fourthChar = fourthCharOTP.getText().toString();
        String fifthChar = fifthCharOTP.getText().toString();
        String sixthChar = sixthCharOTP.getText().toString();

        return (firstChar + secondChar + thirdChar + fourthChar + fifthChar + sixthChar);
    }

    public class OTPTextWatcher implements TextWatcher {
        private View view;

        private OTPTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            String text = editable.toString();

            switch (view.getId()) {
                case R.id.first_char_otp:
                    if (text.length() == 1) {
                        secondCharOTP.requestFocus();
                    } else if (text.length() == 6) {
                        firstCharOTP.setText(String.valueOf(text.charAt(0)));
                        secondCharOTP.setText(String.valueOf(text.charAt(1)));
                        thirdCharOTP.setText(String.valueOf(text.charAt(2)));
                        fourthCharOTP.setText(String.valueOf(text.charAt(3)));
                        fifthCharOTP.setText(String.valueOf(text.charAt(4)));
                        sixthCharOTP.setText(String.valueOf(text.charAt(5)));
                    } else if (text.length() != 0) {
                        firstCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
                    break;
                case R.id.second_char_otp:
                    if (text.length() == 1) {
                        thirdCharOTP.requestFocus();
                    } else if (text.length() == 0) {
                        firstCharOTP.requestFocus();
                    } else {
                        secondCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
                    break;
                case R.id.third_char_otp:
                    if (text.length() == 1) {
                        fourthCharOTP.requestFocus();
                    } else if (text.length() == 0) {
                        secondCharOTP.requestFocus();
                    } else {
                        thirdCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
                    break;
                case R.id.fourth_char_otp:
                    if (text.length() == 1) {
                        fifthCharOTP.requestFocus();
                    } else if (text.length() == 0) {
                        thirdCharOTP.requestFocus();
                    } else {
                        fourthCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
                    break;
                case R.id.fifth_char_otp:
                    if (text.length() == 1) {
                        sixthCharOTP.requestFocus();
                    } else if (text.length() == 0) {
                        fourthCharOTP.requestFocus();
                    } else {
                        fifthCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
                    break;
                case R.id.sixth_char_otp:
                    if (text.length() == 0) {
                        fifthCharOTP.requestFocus();
                    } else if (text.length() != 1) {
                        sixthCharOTP.setText(String.valueOf(text.charAt(0)));
                    }
            }
        }
    }

}
