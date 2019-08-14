package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theindiecorp.grocera.CheckoutActivity;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.EditAddressActivity;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class AddressEditAdapter extends RecyclerView.Adapter<AddressEditAdapter.MyViewHolder> {

    private ArrayList<Address> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context;
    CoordinatorLayout layout;

    public int setAddresses(ArrayList<Address> dataSet){
        this.dataSet =dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView name,address;
        private Button editBtn,deleteBtn;

        public MyViewHolder (View itemView){
            super(itemView);
            this.name = itemView.findViewById(R.id.address_edit_item_name);
            this.address = itemView.findViewById(R.id.address_edit_item_address);
            this.editBtn = itemView.findViewById(R.id.address_edit_item_edit_btn);
            this.deleteBtn = itemView.findViewById(R.id.address_edit_item_delete_btn);
        }
    }

    public AddressEditAdapter(ArrayList<Address> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public AddressEditAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_address_item,parent,false);

        AddressEditAdapter.MyViewHolder myViewHolder = new AddressEditAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final AddressEditAdapter.MyViewHolder holder, int listPosition) {
        final Address address = dataSet.get(listPosition);
        holder.name.setText(address.getName());
        holder.address.setText(address.getAddress());

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, EditAddressActivity.class).putExtra("addressId",address.getId()));
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(address.getId()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}