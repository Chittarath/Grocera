package com.theindiecorp.grocera.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.theindiecorp.grocera.CheckoutActivity;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.Notification;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.MainActivity;
import com.theindiecorp.grocera.OrderViewActivity;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;
import java.util.Calendar;

public class RecentOrdersAdapter extends RecyclerView.Adapter<RecentOrdersAdapter.MyViewHolder> {
    private ArrayList<OrderDetails> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public int setOrders(ArrayList<OrderDetails> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView shopName,address,amount,items,date;
        private Button cancelOrderBtn,reorderBtn;

        public MyViewHolder(View itemView){
            super(itemView);
            this.shopName = itemView.findViewById(R.id.recent_order_item_shop_name);
            this.address = itemView.findViewById(R.id.recent_order_item_shop_address);
            this.amount = itemView.findViewById(R.id.recent_order_item_amount);
            this.items = itemView.findViewById(R.id.recent_order_item_bought_items);
            this.date = itemView.findViewById(R.id.recent_order_item_date);
            this.cancelOrderBtn = itemView.findViewById(R.id.recent_order_item_cancel_btn);
            this.reorderBtn = itemView.findViewById(R.id.recent_order_item_reorder_btn);
        }
    }

    public RecentOrdersAdapter(ArrayList<OrderDetails> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recent_orders_item,viewGroup,false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final OrderDetails orderDetails = dataSet.get(listPosition);
        holder.date.setText(orderDetails.getDate());
        holder.amount.setText("Rs. " + orderDetails.getAmountPayable());
        databaseReference.child("shopDetails").child(orderDetails.getShopId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                holder.shopName.setText(dataSnapshot.child("name").getValue(String.class));
                holder.address.setText(dataSnapshot.child("address").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        holder.items.setText("");

        for(int i=0;i < orderDetails.getCart().size();i++){
            final CartDetails c = orderDetails.getCart().get(i);
            databaseReference.child("productDetails").child(c.getProductId()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    holder.items.setText(holder.items.getText() + dataSnapshot.child("name").getValue(String.class) + "x" + c.getQuantity() + ", ");
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        holder.reorderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to reorder?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        placeOrder(orderDetails);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        holder.cancelOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to cancel the order?");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        databaseReference.child("orderDetails").child(orderDetails.getOrderId()).child("status").setValue("Cancelled");
                        Notification notification = new Notification();
                        notification.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        notification.setOrderId(orderDetails.getOrderId());
                        notification.setType("Cancelled");
                        databaseReference.child("notifications").child(orderDetails.getShopId()).child(orderDetails.getOrderId()).setValue(notification);
                        databaseReference.child("notifications").child(orderDetails.getShopId()).child(orderDetails.getOrderId()).child("content").setValue("Order Cancelled");
                    }
                });
                builder.setNegativeButton("Go back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        if(orderDetails.getStatus().equals("Order Placed")){
            holder.cancelOrderBtn.setVisibility(View.VISIBLE);
            holder.reorderBtn.setVisibility(View.GONE);
        }
        else{
            holder.cancelOrderBtn.setVisibility(View.GONE);
            holder.reorderBtn.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, OrderViewActivity.class)
                        .putExtra("orderId",orderDetails.getOrderId())
                        .putExtra("shopId",orderDetails.getShopId())
                        .putExtra("addressId",orderDetails.getAddress().getId()));
            }
        });
    }

    private void placeOrder(OrderDetails orderDetails) {
        ArrayList<CartDetails> cartDetails = orderDetails.getCart();

        for(CartDetails c : cartDetails){
            databaseReference.child("cartDetails").child(MainActivity.userId).child(c.getProductId()).setValue(c);
        }
        Intent intent = new Intent(context, CheckoutActivity.class);
        intent.putExtra("shopId", orderDetails.getShopId());
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
