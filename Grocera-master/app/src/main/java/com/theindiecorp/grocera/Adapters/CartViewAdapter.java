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

public class CartViewAdapter extends RecyclerView.Adapter<CartViewAdapter.MyViewHolder>{

    private ArrayList<CartDetails> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    Context context;
    CoordinatorLayout layout;
    private int stock;
    private Double price,discount;

    public int setProducts(ArrayList<CartDetails> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView productName,quantity,price,discount;
        private Button addBtn,removeBtn;
        private ImageView productImage;

        public MyViewHolder(View itemView){
            super(itemView);
            this.productName = itemView.findViewById(R.id.cart_view_item_product_name);
            this.quantity = itemView.findViewById(R.id.cart_view_quantity);
            this.discount = itemView.findViewById(R.id.cart_view_item_sale);
            this.price = itemView.findViewById(R.id.cart_view_item_price);
            this.addBtn = itemView.findViewById(R.id.cart_view_item_add);
            this.removeBtn = itemView.findViewById(R.id.cart_view_item_subtract);
            this.productImage = itemView.findViewById(R.id.product_image);
        }
    }

    public CartViewAdapter(ArrayList<CartDetails> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cart_view_item,parent,false);

        CartViewAdapter.MyViewHolder myViewHolder = new CartViewAdapter.MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final CartDetails cart = dataSet.get(listPosition);
        final ProductDetails[] productDetails = {new ProductDetails()};
        String id = cart.getProductId();
        databaseReference.child("productDetails").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productDetails[0] = dataSnapshot.getValue(ProductDetails.class);
                holder.productName.setText(dataSnapshot.child("name").getValue(String.class));
                holder.price.setText( "Rs." + dataSnapshot.child("price").getValue(Double.class));
                holder.discount.setText(dataSnapshot.child("discount").getValue(Double.class) + "% OFF");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseStorage.getInstance().getReference().child("products/" + cart.getShopId() + "/" + cart.getProductId() + "/photo").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context.getApplicationContext()).load(uri).into(holder.productImage);
            }
        });

        holder.quantity.setText(cart.getQuantity() + "");

        databaseReference.child("productDetails").child(cart.getProductId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    stock = dataSnapshot.child("Stock").getValue(Integer.class);
                    if(stock == 0){
                        holder.addBtn.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cart.setQuantity(cart.getQuantity()+1);
                databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cart.getProductId()).child("quantity").setValue(cart.getQuantity());
                databaseReference.child("productDetails").child(cart.getProductId())
                        .child("Stock").setValue(stock - 1);
            }
        });

        holder.removeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cart.setQuantity(cart.getQuantity()-1);
                databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cart.getProductId()).child("quantity").setValue(cart.getQuantity());
                databaseReference.child("productDetails").child(cart.getProductId())
                        .child("Stock").setValue(stock + 1);
            }
        });

        if(cart.getQuantity() == 0){
            dataSet.remove(listPosition);
            databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child(cart.getProductId()).removeValue();
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
