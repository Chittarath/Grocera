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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.RecentOrdersAdapter;
import com.theindiecorp.grocera.Data.OrderDetails;

import java.util.ArrayList;

public class RecentOrdersActivity extends AppCompatActivity {

    ArrayList<OrderDetails> orderDetails = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_orders);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final RecyclerView recentOrdersList = findViewById(R.id.profile_view_past_orders);
        recentOrdersList.setLayoutManager(new LinearLayoutManager(this));

        orderDetails = new ArrayList<>();

        final RecentOrdersAdapter recentOrdersAdapter = new RecentOrdersAdapter(new ArrayList<OrderDetails>(),this);
        recentOrdersList.setAdapter(recentOrdersAdapter);

        Query query = databaseReference.child("orderDetails");
        query.orderByChild("userId").equalTo(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    orderDetails = new ArrayList<>();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        OrderDetails o = snapshot.getValue(OrderDetails.class);
                        o.setOrderId(snapshot.getKey());
                        orderDetails.add(o);
                    }
                    recentOrdersAdapter.setOrders(orderDetails);
                    recentOrdersAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Button returnBtn = findViewById(R.id.go_back_btn);
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RecentOrdersActivity.this,MainActivity.class));
            }
        });
    }
}
