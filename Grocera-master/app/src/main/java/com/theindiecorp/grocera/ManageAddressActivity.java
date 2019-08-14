package com.theindiecorp.grocera;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.AddressEditAdapter;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Fragments.AddAddressFragment;

import java.util.ArrayList;

public class ManageAddressActivity extends AppCompatActivity {

    private Button addAddressBtn;
    ArrayList<Address> addresses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_address);

        addAddressBtn = findViewById(R.id.manage_address_add_btn);
        final FrameLayout frameLayout = findViewById(R.id.container);

        final RecyclerView manageAddressRecycler = findViewById(R.id.edit_address_recycler);
        manageAddressRecycler.setLayoutManager(new LinearLayoutManager(this));

        final AddressEditAdapter addressEditAdapter = new AddressEditAdapter(new ArrayList<Address>(),this);
        manageAddressRecycler.setAdapter(addressEditAdapter);

        addresses = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                addresses = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Address a = snapshot.getValue(Address.class);
                    a.setId(snapshot.getKey());
                    addresses.add(a);
                }
                addressEditAdapter.setAddresses(addresses);
                addressEditAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadFragment(new AddAddressFragment());

        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(frameLayout.getVisibility() == View.GONE){
                    frameLayout.setVisibility(View.VISIBLE);
                    manageAddressRecycler.setVisibility(View.GONE);
                }
                else{
                    frameLayout.setVisibility(View.GONE);
                    manageAddressRecycler.setVisibility(View.VISIBLE    );
                }
            }
        });
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.container,fragment).commit();
            return true;
        }
        return false;
    }
}
