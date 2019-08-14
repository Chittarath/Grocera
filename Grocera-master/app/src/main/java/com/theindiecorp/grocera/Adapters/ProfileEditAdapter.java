/*
package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
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
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Data.User;
import com.theindiecorp.grocera.EditAddressActivity;
import com.theindiecorp.grocera.EditProfileActivity;
import com.theindiecorp.grocera.R;

import org.w3c.dom.NameList;

import java.util.ArrayList;
import java.util.jar.Attributes;

public class ProfileEditAdapter extends RecyclerView.Adapter<ProfileEditAdapter.MyViewHolder> {

    private ArrayList<User> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context;
    CoordinatorLayout layout;

    public int setName(ArrayList<User> dataSet){
        this.dataSet =dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView name,mobile,pincode;
        private Button editBtn,deleteBtn;

        public MyViewHolder (View itemView){
            super(itemView);
            this.name = itemView.findViewById(R.id.profile_edit_item_name);
            this.mobile = itemView.findViewById(R.id.profile_edit_item_mobile);
            this.pincode=itemView.findViewById(R.id.profile_edit_item_pin);
            this.editBtn = itemView.findViewById(R.id.profile_edit_item_edit_btn);
            this.deleteBtn = itemView.findViewById(R.id.profile_edit_item_delete_btn);
        }
    }

    public ProfileEditAdapter(ArrayList<User> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public ProfileEditAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.edit_profile_item,parent,false);

        ProfileEditAdapter.MyViewHolder myViewHolder = new ProfileEditAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {

    }

    @Override
    public void onBindViewHolder(@NonNull final ProfileEditAdapter.MyViewHolder holder, int listPosition) {
        final User user = dataSet.get(listPosition);
        holder.name.setText(user.getName());
        holder.mobile.setText(user.getPhone());
        holder.mobile.setText(user.getPinCode());

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, EditProfileActivity.class).putExtra("addressId",user.getId()));
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                databaseReference.child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(user.getId()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}*/
