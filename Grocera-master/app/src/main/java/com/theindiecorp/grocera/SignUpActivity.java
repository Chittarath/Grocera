package com.theindiecorp.grocera;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theindiecorp.grocera.Data.User;

public class SignUpActivity extends AppCompatActivity {

    public static final int PICK_IMAGE = 100;
    private EditText inputEmail, inputPassword, inputName, inputPin, inputPhone;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        btnSignIn = findViewById(R.id.signup_log_in_btn);
        btnSignUp = findViewById(R.id.signup_register_btn);
        inputEmail = findViewById(R.id.reset_email);
        inputName = findViewById(R.id.signup_display_name);
        inputPassword = findViewById(R.id.login_password);
        inputPin = findViewById(R.id.pin_code);
        inputPhone = findViewById(R.id.phone_number);
        progressBar = findViewById(R.id.progressBar);
        btnResetPassword = findViewById(R.id.reset_password_reset_btn);

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, ResetActivity.class));
            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (inputName.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please provide a Display Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputPin.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please provide a PIN Code", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (inputPhone.getText().toString().length() == 0) {
                    inputPhone.setText("Not Provided");
                    return;
                }


                progressBar.setVisibility(View.VISIBLE);
                //create user

                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignUpActivity.this, "Authentication failed." + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    String id = auth.getCurrentUser().getUid();
                                    User user = new User();
                                    User privateUser = new User();
                                    user.setEmail(email);
                                    user.setId(id);
                                    user.setName(inputName.getText().toString());
                                    user.setPinCode(inputPin.getText().toString());
                                    user.setPhone(inputPhone.getText().toString());

                                    writeNewUser(user, id);
                                    updatePrivateInfo(privateUser, id);
                                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                    intent.putExtra("name", auth.getCurrentUser().getDisplayName());
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });

            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void writeNewUser(User user, String id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(id).setValue(user);
    }

    private void updatePrivateInfo(User user, String id) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("privateData").child(id).setValue(user);
    }
}