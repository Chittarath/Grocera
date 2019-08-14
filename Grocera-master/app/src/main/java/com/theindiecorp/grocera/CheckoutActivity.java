package com.theindiecorp.grocera;

import android.Manifest;
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
import com.theindiecorp.grocera.Adapters.AddressViewAdapter;
import com.theindiecorp.grocera.Adapters.CartViewAdapter;
import com.theindiecorp.grocera.Data.Address;
import com.theindiecorp.grocera.Data.CartDetails;
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

public class CheckoutActivity extends AppCompatActivity {

    ArrayList<CartDetails> cart;
    ArrayList<Address> addresses;
    public static String PAYMENT_TYPE = "";
    public static Address Address;
    Double total=0d,discount=0d,deliveryFee = 0d,toPay = 0d;

    private TextView totalTv,discountTv,deliveryFeeTv,toPayTv;
    private Dialog loadingDialog, paymentmethodDialog;
    private ImageButton paytm, cod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        final String shopId = getIntent().getStringExtra("shopId");

        final LinearLayout addressBox = findViewById(R.id.address_box);
        final FrameLayout addAddressFrameLayout = findViewById(R.id.checkout_add_address_fragment_container);
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        totalTv = findViewById(R.id.checkout_item_total_tv);
        discountTv = findViewById(R.id.checkout_item_discount_tv);
        deliveryFeeTv = findViewById(R.id.checkout_item_delivery_fee_tv);
        toPayTv = findViewById(R.id.checkout_to_pay_tv);

        RecyclerView recyclerView = findViewById(R.id.checkout_product_list);

        loadingDialog=new Dialog(CheckoutActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        paymentmethodDialog=new Dialog(CheckoutActivity.this);
        paymentmethodDialog.setContentView(R.layout.payment_method);
        paymentmethodDialog.setCancelable(true);
      /*  paymentmethodDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));*/
        paymentmethodDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        paytm=paymentmethodDialog.findViewById(R.id.paytm_btn);
        cod = paymentmethodDialog.findViewById(R.id.cod_btn);

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
                paymentmethodDialog.show();

            }
        });

        cod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PAYMENT_TYPE = "OFFLINE";
            }
        });

        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paymentmethodDialog.dismiss();
                loadingDialog.show();
                if (ContextCompat.checkSelfPermission(CheckoutActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CheckoutActivity.this, new String[]{Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS}, 101);
                }

                final String M_id="hlisbA90705689498446";
                final String customer_id= FirebaseAuth.getInstance().getUid();
                final String order_id= UUID.randomUUID().toString().substring(0,28);
                String url="https://outward-bound-radio.000webhostapp.com/paytm/paytm/generateChecksum.php";
                final String callbackurl="https://pguat.paytm.com/paytmchecksum/paytmCallback.jsp";

                RequestQueue requestQueue = Volley.newRequestQueue(CheckoutActivity.this);
                StringRequest stringRequest=new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject=new JSONObject(response);

                            if (jsonObject.has("CHECKSUMHASH")){
                                String CHECKSUMHASH=jsonObject.getString("CHECKSUMHASH");

                                PaytmPGService paytmPGService=PaytmPGService.getStagingService();
                                HashMap<String, String> paramMap = new HashMap<String,String>();
                                paramMap.put( "MID" , M_id);
                                paramMap.put( "ORDER_ID" , order_id);
                                paramMap.put( "CUST_ID" , customer_id);
                                paramMap.put( "CHANNEL_ID" , "WAP");
                                paramMap.put( "TXN_AMOUNT" , "100.00");
                                paramMap.put( "WEBSITE" , "WEBSTAGING");
                                paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                                paramMap.put( "CALLBACK_URL", callbackurl);
                                paramMap.put("CHECKSUMHASH",CHECKSUMHASH);

                                PaytmOrder paytmOrder=new PaytmOrder(paramMap);
                                paytmPGService.initialize(paytmOrder,null);
                                paytmPGService.startPaymentTransaction(CheckoutActivity.this, true, true, new PaytmPaymentTransactionCallback() {
                                    @Override
                                    public void onTransactionResponse(Bundle inResponse) {
                                        Toast.makeText(getApplicationContext(), "Payment Transaction response " + inResponse.toString(), Toast.LENGTH_LONG).show();
                                    }


                                    @Override
                                    public void networkNotAvailable() {
                                        Toast.makeText(getApplicationContext(), "Network connection error: Check your internet connectivity", Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void clientAuthenticationFailed(String inErrorMessage) {
                                        Toast.makeText(getApplicationContext(), "Authentication failed: Server error" + inErrorMessage.toString(), Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void someUIErrorOccurred(String inErrorMessage) {
                                        Toast.makeText(getApplicationContext(), "UI Error " + inErrorMessage , Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onErrorLoadingWebPage(int iniErrorCode, String inErrorMessage, String inFailingUrl) {

                                        Toast.makeText(getApplicationContext(), "Unable to load webpage " + inErrorMessage.toString(), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onBackPressedCancelTransaction() {
                                        Toast.makeText(getApplicationContext(), "Transaction cancelled" , Toast.LENGTH_LONG).show();

                                    }

                                    @Override
                                    public void onTransactionCancel(String inErrorMessage, Bundle inResponse) {
                                        Toast.makeText(getApplicationContext(), "Transaction cancelled", Toast.LENGTH_SHORT).show();

                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();
                        Toast.makeText(CheckoutActivity.this, "something went wrong!", Toast.LENGTH_SHORT).show();

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String, String> paramMap = new HashMap<String,String>();
                        paramMap.put( "MID" , M_id);
                        paramMap.put( "ORDER_ID" , order_id);
                        paramMap.put( "CUST_ID" , customer_id);
                        paramMap.put( "CHANNEL_ID" , "WAP");
                        paramMap.put( "TXN_AMOUNT" ,"100.00");
                        paramMap.put( "WEBSITE" , "WEBSTAGING");
                        paramMap.put( "INDUSTRY_TYPE_ID" , "Retail");
                        paramMap.put( "CALLBACK_URL", callbackurl);
                        return paramMap;
                    }
                };
                requestQueue.add(stringRequest);


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

                        Calendar calendar = Calendar.getInstance();
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int year = calendar.get(Calendar.YEAR);
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        String date = day + "/" + month + "/" + year + ", " + hour + ":" + minute;

                        OrderDetails orderDetails = new OrderDetails();
                        orderDetails.setStatus("Placed");
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
                        placeOrder(orderDetails);
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

    private void calculateFee(ArrayList<CartDetails> cart) {
        if(!cart.isEmpty()){
            total = 0d;
            discount = 0d;
            toPay = 0d;
            for(int i = 0 ;i < cart.size(); i++){
                CartDetails c = cart.get(i);
                Double amt = c.getPricePerPiece() * c.getQuantity();
                Double disc = amt * (c.getDiscount()/100) ;
                total = total + amt;
                discount = discount + (disc);
            }
            toPay = total - discount + deliveryFee;
        }
    }

    private void placeOrder(OrderDetails orderDetails){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        String id = databaseReference.push().getKey();
        databaseReference.child("orderDetails").child(id).setValue(orderDetails);
        databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue();
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

    @Override
    protected void onPause() {
        super.onPause();
        loadingDialog.dismiss();
    }
}
