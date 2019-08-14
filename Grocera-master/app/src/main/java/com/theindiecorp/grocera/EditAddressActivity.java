package com.theindiecorp.grocera;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Data.Address;

public class EditAddressActivity extends AppCompatActivity {

    private TextView name,address;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_address);

        final String addressId = getIntent().getStringExtra("addressId");

        name = findViewById(R.id.edit_address_name_et);
        address = findViewById(R.id.edit_address_address_et);
        submitBtn = findViewById(R.id.edit_address_submit_btn);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(addressId).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        name.setText(dataSnapshot.child("name").getValue(String.class));
                        address.setText(dataSnapshot.child("address").getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Address a = new Address();
                a.setName(name.getText().toString());
                a.setAddress(address.getText().toString());
                a.setId(addressId);
                databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(addressId).setValue(a);

                startActivity(new Intent(EditAddressActivity.this,ManageAddressActivity.class));
                finish();
            }
        });
    }
}
