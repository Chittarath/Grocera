package com.theindiecorp.grocera;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paytm.pgsdk.PaytmOrder;
import com.paytm.pgsdk.PaytmPGService;
import com.paytm.pgsdk.PaytmPaymentTransactionCallback;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;
import com.theindiecorp.grocera.Adapters.AddressViewAdapter;
import com.theindiecorp.grocera.Adapters.CartViewAdapter;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.Notification;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.Fragments.AddAddressFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class CheckoutActivity extends AppCompatActivity implements PaymentResultListener {

    ArrayList<CartDetails> cart;
    ArrayList<Address> addresses;
    public static String PAYMENT_TYPE = "", shopId;
    public static Address Address;
    Double total=0d,discount=0d,deliveryFee = 0d,toPay = 0d;

    private TextView totalTv,discountTv,deliveryFeeTv,toPayTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        PAYMENT_TYPE = "";
        Address = null;

        shopId = getIntent().getStringExtra("shopId");

        final LinearLayout addressBox = findViewById(R.id.address_box);
        final FrameLayout addAddressFrameLayout = findViewById(R.id.checkout_add_address_fragment_container);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        totalTv = findViewById(R.id.checkout_item_total_tv);
        discountTv = findViewById(R.id.checkout_item_discount_tv);
        deliveryFeeTv = findViewById(R.id.checkout_item_delivery_fee_tv);
        toPayTv = findViewById(R.id.checkout_to_pay_tv);

        RecyclerView recyclerView = findViewById(R.id.checkout_product_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final RecyclerView addressRecycler = findViewById(R.id.checkout_address_list);
        addressRecycler.setLayoutManager(new LinearLayoutManager(this));

        cart = new ArrayList<>();
        addresses = new ArrayList<>();

        final CartViewAdapter adapter = new CartViewAdapter(new ArrayList<CartDetails>(),this);
        recyclerView.setAdapter(adapter);

        final AddressViewAdapter addressViewAdapter = new AddressViewAdapter(new ArrayList<Address>(),this);
        addressRecycler.setAdapter(addressViewAdapter);

        databaseReference.child("address").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    addresses = new ArrayList<>();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Address a = snapshot.getValue(Address.class);
                        a.setId(snapshot.getKey());
                        addresses.add(a);
                    }
                    addressViewAdapter.setAddresses(addresses);
                    addressViewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cart = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    CartDetails c = snapshot.getValue(CartDetails.class);
                    cart.add(c);
                }
                if(cart.isEmpty()){
                    startActivity(new Intent(CheckoutActivity.this, MainActivity.class));
                }
                calculateFee(cart);
                adapter.setProducts(cart);
                totalTv.setText("Rs." + total);
                discountTv.setText("- Rs." +discount);
                toPayTv.setText("Rs." + toPay);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("shopDetails").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                deliveryFeeTv.setText("Rs." + dataSnapshot.child("deliveryFee").getValue(Double.class));
                deliveryFee = dataSnapshot.child("deliveryFee").getValue(Double.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button selectAddressBtn = findViewById(R.id.checkout_select_address_btn);
        selectAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addressBox.getVisibility() == View.GONE){
                    addressBox.setVisibility(View.VISIBLE);
                }
                else{
                    addressBox.setVisibility(View.GONE);
                }
            }
        });

        loadFragment(new AddAddressFragment());

        Button addAddressBtn = findViewById(R.id.checkout_add_address_btn);
        addAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(addAddressFrameLayout.getVisibility() == View.GONE){
                    addAddressFrameLayout.setVisibility(View.VISIBLE);
                }else{
                    addAddressFrameLayout.setVisibility(View.GONE);
                }
            }
        });

        Button showPaymentsBtn = findViewById(R.id.checkout_payment_method);
        showPaymentsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = onCreateDialog();
                dialog.show();
            }
        });

        Button placeOrderBtn = findViewById(R.id.checkout_place_order_btn);
        placeOrderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CheckoutActivity.this);
                builder.setMessage("Order Confirmed");
                builder.setCancelable(true);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(PAYMENT_TYPE.isEmpty()){
                            Toast.makeText(CheckoutActivity.this,"Please select a payment method",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(Address == null){
                            Toast.makeText(CheckoutActivity.this,"Please select a delivery address",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(PAYMENT_TYPE.equals("OFFLINE")){
                            placeOrder();
                            dialog.dismiss();
                        }
                        else{
                            startPayment(toPay);
                        }
                    }
                });
                builder.setNegativeButton("Go Back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

    }

    private Dialog onCreateDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.payment_method);

        ImageButton codBtn = dialog.findViewById(R.id.cod_btn);
        ImageButton onlinePaymentBtn = dialog.findViewById(R.id.paytm_btn);

        codBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PAYMENT_TYPE = "OFFLINE";
                dialog.dismiss();
            }
        });

        onlinePaymentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PAYMENT_TYPE = "ONLINE";
                dialog.dismiss();
            }
        });

        return dialog;
    }

    private void calculateFee(ArrayList<CartDetails> cart) {
        if(!cart.isEmpty()){
            total = 0d;
            discount = 0d;
            toPay = 0d;
            for(int i = 0 ;i < cart.size(); i++){
                CartDetails c = cart.get(i);
                Double amt = c.getPricePerPiece() * c.getQuantity();
                double disc = amt * (c.getDiscount()/100) ;
                total = total + amt;
                discount = discount + (disc);
            }
            toPay = total - discount + deliveryFee;
        }
    }

    private void placeOrder(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        String date = day + "/" + month + "/" + year + ", " + hour + ":" + minute;

        OrderDetails orderDetails = new OrderDetails();
        orderDetails.setStatus("Order Placed");
        orderDetails.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        orderDetails.setCart(cart);
        orderDetails.setAddress(Address);
        orderDetails.setModeOfPayment(PAYMENT_TYPE);
        orderDetails.setAmountPayable(toPay);
        orderDetails.setDate(date);
        orderDetails.setShopId(shopId);
        orderDetails.setTotal(total);
        orderDetails.setDeliveryFee(deliveryFee);
        orderDetails.setDiscount(discount);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String id = databaseReference.push().getKey();
        orderDetails.setOrderId(id);
        databaseReference.child("orderDetails").child(id).setValue(orderDetails);
        databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();

        Notification notification = new Notification();
        notification.setUserId(FirebaseAuth.getInstance().getCurrentUser().getUid());
        notification.setOrderId(id);
        notification.setType("Placed");
        databaseReference.child("notifications").child(orderDetails.getShopId()).child(id).setValue(notification);
        databaseReference.child("notifications").child(orderDetails.getShopId()).child(id).child("content").setValue("You have a new order");

        startActivity(new Intent(CheckoutActivity.this,OrderPlaced.class));
        finish();
    }

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.checkout_add_address_fragment_container,fragment).commit();
            return true;
        }
        return false;
    }

    private void startPayment(Double amount){
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_hm9uR6b7qPqtH6");
        checkout.setImage(R.mipmap.ic_launcher);

        Activity activity = this;

        try {
            JSONObject options = new JSONObject();
            options.put("currency", "INR");
            options.put("amount", amount * 100);
            checkout.open(activity, options);
        } catch (JSONException e) {
            Log.d("ErrorPayment",  e.getMessage());
            Toast.makeText(activity, "Error in payment: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onPaymentSuccess(String s) {
        placeOrder();
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed. Please try again later.", Toast.LENGTH_SHORT).show();
    }
}
