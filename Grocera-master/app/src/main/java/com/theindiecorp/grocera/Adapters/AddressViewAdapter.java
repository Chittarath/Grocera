package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theindiecorp.grocera.CheckoutActivity;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class AddressViewAdapter extends RecyclerView.Adapter<AddressViewAdapter.MyViewHolder> {

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

        public MyViewHolder (View itemView){
            super(itemView);
            this.name = itemView.findViewById(R.id.address_item_name);
            this.address = itemView.findViewById(R.id.address_item_address);
        }
    }

    public AddressViewAdapter(ArrayList<Address> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.address_item,parent,false);

        AddressViewAdapter.MyViewHolder myViewHolder = new AddressViewAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final Address address = dataSet.get(listPosition);
        holder.name.setText(address.getName());
        holder.address.setText(address.getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckoutActivity.Address = address;
                holder.itemView.setBackgroundColor(holder.itemView.getResources().getColor(R.color.colorPrimary));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
