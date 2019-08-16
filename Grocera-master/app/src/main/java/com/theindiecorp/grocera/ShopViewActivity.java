package com.theindiecorp.grocera;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.theindiecorp.grocera.Adapters.CategoryAdapter;
import com.theindiecorp.grocera.Adapters.ShopViewAdapter;
import com.theindiecorp.grocera.Data.ProductDetails;

import java.util.ArrayList;

public class ShopViewActivity extends AppCompatActivity {

    ArrayList<ProductDetails> productDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_view);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String shopId = getIntent().getStringExtra("shopId");

        final TextView shopName = findViewById(R.id.shop_name);
        final ImageView profilePic = findViewById(R.id.shop_image);
        final TextView discountTxt = findViewById(R.id.discount_txt);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final CategoryAdapter categoryAdapter = new CategoryAdapter(this,shopId,new ArrayList<String>());
        recyclerView.setAdapter(categoryAdapter);

        databaseReference.child("shopDetails").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shopName.setText(dataSnapshot.child("name").getValue(String.class));
                if(dataSnapshot.child("discount").exists()){
                    discountTxt.setText(dataSnapshot.child("discount").getValue(Double.class) + "% OFF");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query = databaseReference.child("productDetails");
        query.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> categories = new ArrayList<>();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ProductDetails p = snapshot.getValue(ProductDetails.class);
                    if(p.getCategory()!=null){
                        categories.add(p.getCategory());
                    }
                }
                categoryAdapter.setCategories(categories);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        FirebaseStorage.getInstance().getReference().child("shops/" + shopId + "/images/profile_pic/profile_pic.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ShopViewActivity.this).load(uri).into(profilePic);
            }
        });


        Button cartViewBtn = findViewById(R.id.shop_view_cart_btn);
        cartViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ShopViewActivity.this,CartViewActivity.class));
            }
        });

        Button goBackBtn = findViewById(R.id.go_back_btn);
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ShopViewActivity.this,MainActivity.class));
                finish();
            }
        });
    }
}
