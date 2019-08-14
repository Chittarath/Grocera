package com.theindiecorp.grocera.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.R;

public class AddAddressFragment extends Fragment {

    private EditText name,address;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_address_fragment,container,false);

        name = view.findViewById(R.id.add_address_name_et);
        address = view.findViewById(R.id.add_address_address_et);

        Button addAddressBtn = view.findViewById(R.id.add_address_submit_btn);
        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                String id = databaseReference.push().getKey();
                databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id).child("name").setValue(name.getText().toString());
                databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(id).child("address").setValue(address.getText().toString());
                name.setText("");
                address.setText("");

            }
        });

        return view;
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
