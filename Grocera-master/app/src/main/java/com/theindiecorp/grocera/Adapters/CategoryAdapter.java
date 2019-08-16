package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Data.ProductDetails;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.MyViewHolder> {
    private Context context;
    private String shopId;
    private ArrayList<String> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public int setCategories(ArrayList<String> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView title;
        private RecyclerView recyclerView;

        public MyViewHolder(View view){
            super(view);
            this.title = view.findViewById(R.id.title);
            this.recyclerView = view.findViewById(R.id.products_recycler);
        }
    }

    public CategoryAdapter (Context context, String shopId, ArrayList<String> dataSet){
        this.context = context;
        this.shopId = shopId;
        this.dataSet = dataSet;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.category_item,parent,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int listPosition) {
        final String category = dataSet.get(listPosition);

        holder.recyclerView.setLayoutManager(new LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false));
        final ShopViewAdapter shopViewAdapter = new ShopViewAdapter(new ArrayList<ProductDetails>(),context);
        final ArrayList<ProductDetails> productDetails = new ArrayList<>();
        holder.recyclerView.setAdapter(shopViewAdapter);

        Query query = databaseReference.child("productDetails");
        query.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        ProductDetails p = snapshot.getValue(ProductDetails.class);
                        if(p.getCategory().equals(category)){
                            productDetails.add(p);
                        }
                    }
                    shopViewAdapter.setProducts(productDetails);
                    shopViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.title.setText(category);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
