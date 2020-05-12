package com.theindiecorp.grocera.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.CartViewAdapter;
import com.theindiecorp.grocera.CheckoutActivity;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.OrderPlaced;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;


public class CartFragment extends Fragment {

    public ArrayList<CartDetails> cart;
    Double total = 0d;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart,container,false);

        final Button checkoutBtn = view.findViewById(R.id.cart_view_checkout_btn);
        final TextView emptyCartText = view.findViewById(R.id.emptyCartScreenText);
        final LinearLayout itemTotal = view.findViewById(R.id.item_total_layout);
        final TextView totalAmountTv = view.findViewById(R.id.checkout_to_pay_tv);
        final TextView myCartTxt = view.findViewById(R.id.cart);
        final TextView pinCodeTxt = view.findViewById(R.id.pin_code);

        RecyclerView recyclerView = view.findViewById(R.id.cart_view_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cart = new ArrayList<>();

        final CartViewAdapter adapter = new CartViewAdapter(new ArrayList<CartDetails>(),getContext());
        recyclerView.setAdapter(adapter);

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("cartDetails").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cart = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    CartDetails c = snapshot.getValue(CartDetails.class);
                    cart.add(c);
                }
                if(cart.isEmpty()){
                    checkoutBtn.setVisibility(View.GONE);
                    itemTotal.setVisibility(View.GONE);
                    emptyCartText.setVisibility(View.VISIBLE);
                    myCartTxt.setText("My Cart");
                }
                else{
                    itemTotal.setVisibility(View.VISIBLE);
                    checkoutBtn.setVisibility(View.VISIBLE);
                    emptyCartText.setVisibility(View.GONE);
                    myCartTxt.setText("My Cart (" + cart.size() + ")");
                }
                calculateTotal(cart);
                totalAmountTv.setText("Rs." + total);
                adapter.setProducts(cart);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        checkoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!cart.isEmpty()){
                    Intent intent = new Intent(getContext(),CheckoutActivity.class);
                    intent.putExtra("shopId",cart.get(0).getShopId());
                    startActivity(intent);
                }
            }
        });

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pinCodeTxt.setText("Pin code : " + dataSnapshot.child("pinCode").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

    private void calculateTotal(ArrayList<CartDetails> cart) {
        if(!cart.isEmpty()){
            total = 0d;
            for(int i = 0 ;i < cart.size(); i++){
                CartDetails c = cart.get(i);
                Double amt = c.getPricePerPiece() * c.getQuantity();
                total = total + amt;
            }
        }
    }
}
