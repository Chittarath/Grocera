package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Data.ProductDetails;
import com.theindiecorp.grocera.R;
import com.theindiecorp.grocera.ShopViewActivity;

import java.util.ArrayList;

public class ProductSearchAdapter extends RecyclerView.Adapter<ProductSearchAdapter.MyViewHolder> {
    private Context context;
    private ArrayList<String> dataSet = new ArrayList<>();

    public int setProducts(ArrayList<String> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView itemName;

        public MyViewHolder(View itemView){
            super(itemView);
            this.itemName = itemView.findViewById(R.id.product_name);
        }
    }

    public ProductSearchAdapter(Context context,ArrayList<String> dataSet){
        this.context = context;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.product_search_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        String productId = dataSet.get(listPosition);
        final ProductDetails[] productDetails = {new ProductDetails()};

        final DatabaseReference[] databaseReference = {FirebaseDatabase.getInstance().getReference()};
        databaseReference[0].child("productDetails").child(productId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productDetails[0] = dataSnapshot.getValue(ProductDetails.class);
                holder.itemName.setText(productDetails[0].getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context,ShopViewActivity.class).putExtra("shopId",productDetails[0].getShopId()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
