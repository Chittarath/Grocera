package com.theindiecorp.grocera.Adapters;

import android.content.Context;
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
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class OrderedItemsAdapter extends RecyclerView.Adapter<OrderedItemsAdapter.MyViewHolder> {
    private ArrayList<CartDetails> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public int setItems(ArrayList<CartDetails> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView itemName, price;

        public MyViewHolder(View itemView){
            super(itemView);
            itemName = itemView.findViewById(R.id.ordered_item_name_tv);
            price = itemView.findViewById(R.id.ordered_item_cost_tv);
        }
    }

    public OrderedItemsAdapter(ArrayList<CartDetails> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_view_ordered_items,viewGroup,false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final CartDetails cart = dataSet.get(listPosition);

        databaseReference.child("productDetails").child(cart.getShopId()).child(cart.getProductId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.itemName.setText(dataSnapshot.child("name").getValue(String.class) + " x" + cart.getQuantity());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Double amt = cart.getPricePerPiece() * cart.getQuantity();

        holder.price.setText("Rs." + amt);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
