package com.theindiecorp.grocera;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.HomeActivityRecycler;
import com.theindiecorp.grocera.Adapters.OrderedItemsAdapter;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.Notification;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.Data.ShopDetails;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderViewActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    TextView shopName,itemTotal,discount,payable,deliveryFee;
    TextView orderIdTv;
    LinearLayout orderPlacedBox, orderConfirmedBox, outForDeliveryBox, orderDeliveredBox, orderCancelBox;

    String shopId,addressId;
    OrderDetails o = new OrderDetails();
    ArrayList<CartDetails> cartDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);

        String orderId = getIntent().getStringExtra("orderId");
        shopId = getIntent().getStringExtra("shopId");
        addressId = getIntent().getStringExtra("addressId");

        orderIdTv = findViewById(R.id.order_id);
        itemTotal = findViewById(R.id.order_view_total_tv);
        discount = findViewById(R.id.order_view_discount_tv);
        payable = findViewById(R.id.order_view_payable_tv);
        deliveryFee = findViewById(R.id.order_view_delivery_fee_tv);
        shopName = findViewById(R.id.shop_name);
        orderPlacedBox = findViewById(R.id.order_placed_box);
        orderConfirmedBox = findViewById(R.id.order_confirmed_box);
        outForDeliveryBox = findViewById(R.id.out_for_delivery_box);
        orderDeliveredBox = findViewById(R.id.order_delivered_box);
        orderCancelBox = findViewById(R.id.order_cancelled_box);

        RecyclerView itemList = findViewById(R.id.order_view_items_list);
        itemList.setLayoutManager(new LinearLayoutManager(this));

        final OrderedItemsAdapter orderedItemsAdapter = new OrderedItemsAdapter(new ArrayList<CartDetails>(),this);
        itemList.setAdapter(orderedItemsAdapter);

        databaseReference.child("orderDetails").child(orderId).child("cart").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartDetails = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    CartDetails c = snapshot.getValue(CartDetails.class);
                    cartDetails.add(c);
                }
                orderedItemsAdapter.setItems(cartDetails);
                orderedItemsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("shopDetails").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shopName.setText(dataSnapshot.child("name").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("orderDetails").child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                o = dataSnapshot.getValue(OrderDetails.class);
                itemTotal.setText("Rs." + o.getTotal());
                discount.setText("-Rs." + o.getDiscount());
                payable.setText("Rs." +o.getAmountPayable());
                deliveryFee.setText("Rs." + o.getDeliveryFee());
                orderIdTv.setText("Order Id : #" + dataSnapshot.getKey());

                if(o.getStatus().equals("Order Placed"))
                {
                    orderPlacedBox.setVisibility(View.VISIBLE);
                    orderConfirmedBox.setVisibility(View.GONE);
                    outForDeliveryBox.setVisibility(View.GONE);
                    orderDeliveredBox.setVisibility(View.GONE);
                    orderCancelBox.setVisibility(View.GONE);
                }
                else if(o.getStatus().equals("Order Confirmed"))
                {
                    orderPlacedBox.setVisibility(View.GONE);
                    orderConfirmedBox.setVisibility(View.VISIBLE);
                    outForDeliveryBox.setVisibility(View.GONE);
                    orderDeliveredBox.setVisibility(View.GONE);
                    orderCancelBox.setVisibility(View.GONE);
                }
                else if(o.getStatus().equals("Out for Delivery")){
                    orderPlacedBox.setVisibility(View.GONE);
                    orderConfirmedBox.setVisibility(View.GONE);
                    outForDeliveryBox.setVisibility(View.VISIBLE);
                    orderDeliveredBox.setVisibility(View.GONE);
                    orderCancelBox.setVisibility(View.GONE);
                }
                else if(o.getStatus().equals("Order Delivered")){
                    orderPlacedBox.setVisibility(View.GONE);
                    orderConfirmedBox.setVisibility(View.GONE);
                    outForDeliveryBox.setVisibility(View.GONE);
                    orderDeliveredBox.setVisibility(View.VISIBLE);
                    orderCancelBox.setVisibility(View.GONE);
                }
                else if(o.getStatus().equals("Cancelled")){
                    orderPlacedBox.setVisibility(View.GONE);
                    orderConfirmedBox.setVisibility(View.GONE);
                    outForDeliveryBox.setVisibility(View.GONE);
                    orderDeliveredBox.setVisibility(View.GONE);
                    orderCancelBox.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button reorderBtn = findViewById(R.id.order_view_reorder_btn);
        reorderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!o.getStatus().equals("Order Placed")){
                    ArrayList<CartDetails> cartDetails = o.getCart();

                    for(CartDetails c : cartDetails){
                        databaseReference.child("cartDetails").child(MainActivity.userId).child(c.getProductId()).setValue(c);
                    }
                    Intent intent = new Intent(OrderViewActivity.this, CheckoutActivity.class);
                    intent.putExtra("shopId", o.getShopId());
                    startActivity(intent);
                }
                else{
                    Toast.makeText(OrderViewActivity.this,"This order is yet to be delivered",Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button goBackBtn = findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OrderViewActivity.this, MainActivity.class));
            }
        });
    }
}
