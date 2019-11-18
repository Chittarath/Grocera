package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.theindiecorp.grocera.R;
import com.theindiecorp.grocera.ShopViewActivity;

import java.util.ArrayList;

public class OffersListAdapter extends RecyclerView.Adapter<OffersListAdapter.MyViewHolder> {
    private ArrayList<String> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public int setShopIds(ArrayList<String> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView offerTxt, nameTxt;
        private ImageView shopImg;

        public MyViewHolder(View itemView){
            super(itemView);
            this.offerTxt = itemView.findViewById(R.id.offer_text);
            this.shopImg = itemView.findViewById(R.id.logo);
            this.nameTxt = itemView.findViewById(R.id.shop_name_text);
        }
    }

    public OffersListAdapter(Context context, ArrayList<String> data){
        this.context = context;
        this.dataSet = data;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.offers_list_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final String shopId = dataSet.get(listPosition);
        databaseReference.child("shopDetails").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.offerTxt.setText(Math.round(dataSnapshot.child("discount").getValue(Double.class)) + "% OFF");
                holder.nameTxt.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseStorage.getInstance().getReference().child("shops/" + shopId + "/images/profile_pic/profile_pic.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.shopImg);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, ShopViewActivity.class)
                        .putExtra("shopId",shopId));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
