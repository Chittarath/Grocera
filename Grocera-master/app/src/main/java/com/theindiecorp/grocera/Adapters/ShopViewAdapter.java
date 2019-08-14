package com.theindiecorp.grocera.Adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.ProductDetails;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class ShopViewAdapter extends RecyclerView.Adapter<ShopViewAdapter.MyViewHolder>{
    private ArrayList<ProductDetails> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    Context context;
    CoordinatorLayout layout;

    public int setProducts(ArrayList<ProductDetails> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView productName,stock,price,discount;
        private Button addBtn;
        private ImageView productPhoto;

        public MyViewHolder(View itemView){
            super(itemView);
            this.productName = itemView.findViewById(R.id.item_product_name);
            this.stock = itemView.findViewById(R.id.item_stock);
            this.price = itemView.findViewById(R.id.item_price);
            this.discount = itemView.findViewById(R.id.home_item_sale);
            this.addBtn = itemView.findViewById(R.id.mechanic_list_choose_garage_btn);
            this.productPhoto = itemView.findViewById(R.id.product_image);
        }
    }

    public ShopViewAdapter(ArrayList<ProductDetails> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    public ShopViewAdapter(ArrayList<ProductDetails> data, Context context,CoordinatorLayout layout){
        this.dataSet = data;
        this.context = context;
        this.layout = layout;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shop_view_item,parent,false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int listPosition) {
        final ProductDetails productDetails = dataSet.get(listPosition);
        holder.productName.setText(productDetails.getName());
        holder.stock.setText(productDetails.getStock() + " left");
        holder.price.setText("Rs." + productDetails.getPrice().toString());
        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CartDetails cart = new CartDetails();
                cart.setQuantity(1);
                cart.setProductId(productDetails.getId());
                cart.setShopId(productDetails.getShopId());
                cart.setPricePerPiece(productDetails.getPrice());
                cart.setDiscount(productDetails.getDiscount());

                databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child(productDetails.getId()).setValue(cart);
                databaseReference.child("productDetails").child(productDetails.getId())
                        .child("Stock").setValue(productDetails.getStock() - 1);
            }
        });

        databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(productDetails.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        holder.addBtn.setText("Added");
                        holder.addBtn.setEnabled(false);
                }
                else{
                    holder.addBtn.setText("Add");
                    holder.addBtn.setEnabled(true);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseStorage.getInstance().getReference().child("products/" + productDetails.getShopId() + "/" + productDetails.getId() + "/photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext()).load(uri).into(holder.productPhoto);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
