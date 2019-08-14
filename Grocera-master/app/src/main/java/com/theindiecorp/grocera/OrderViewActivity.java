package com.theindiecorp.grocera;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.OrderedItemsAdapter;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.Data.ShopDetails;

import java.util.ArrayList;
import java.util.Calendar;

public class OrderViewActivity extends AppCompatActivity {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private TextView shopName,shopAddress,userName,userAddress,status,dateTv,itemTotal,discount,payable,deliveryFee;
    String shopId,addressId;
    OrderDetails o = new OrderDetails();
    ArrayList<CartDetails> cartDetails = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_view);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final String orderId = getIntent().getStringExtra("orderId");
        shopId = getIntent().getStringExtra("shopId");
        addressId = getIntent().getStringExtra("addressId");

        final TextView orderIdTv = findViewById(R.id.order_id);
        orderIdTv.setText("Order Id : #" + orderId);
        itemTotal = findViewById(R.id.order_view_total_tv);
        discount = findViewById(R.id.order_view_discount_tv);
        payable = findViewById(R.id.order_view_payable_tv);
        deliveryFee = findViewById(R.id.order_view_delivery_fee_tv);

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

        databaseReference.child("orderDetails").child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                o = dataSnapshot.getValue(OrderDetails.class);
                itemTotal.setText("Rs." + o.getTotal());
                discount.setText("-Rs." + o.getDiscount());
                payable.setText("Rs." +o.getAmountPayable());
                deliveryFee.setText("Rs." + o.getDeliveryFee());
                orderIdTv.setText("Order Id : #" + o.getOrderId());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button reorderBtn = findViewById(R.id.order_view_reorder_btn);
        reorderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!o.getStatus().equals("Placed")){
                    String id = databaseReference.push().getKey();

                    Calendar calendar = Calendar.getInstance();
                    int day = calendar.get(Calendar.DAY_OF_MONTH);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    int year = calendar.get(Calendar.YEAR);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    String date = day + "/" + month + "/" + year + ", " + hour + ":" + minute;
                    o.setDate(date);
                    o.setStatus("Placed");
                    o.setOrderId(id);

                    databaseReference.child("orderDetails").child(id).setValue(o);
                }
                else{
                    Toast.makeText(OrderViewActivity.this,"This order is yet to be delivered",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
