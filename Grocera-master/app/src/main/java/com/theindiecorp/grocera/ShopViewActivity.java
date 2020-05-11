package com.theindiecorp.grocera;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
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
import com.theindiecorp.grocera.Adapters.ShopViewAdapter;
import com.theindiecorp.grocera.Data.ProductDetails;

import java.util.ArrayList;

public class ShopViewActivity extends AppCompatActivity {

    ArrayList<ProductDetails> productDetails;
    ArrayList<String> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_view);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        String shopId = getIntent().getStringExtra("shopId");

        final TextView shopName = findViewById(R.id.shop_name);
        final ImageView profilePic = findViewById(R.id.shop_image);
        final TextView discountTxt = findViewById(R.id.discount_txt);
        FloatingActionButton fab = findViewById(R.id.fab);
        Button cartViewBtn = findViewById(R.id.shop_view_cart_btn);

        final View dialogView = LayoutInflater.from(this).inflate(R.layout.filter_fab_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final ListView dialogListView = dialogView.findViewById(R.id.list);
        builder.setView(dialogView);

        ArrayAdapter<String> dialogListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        dialogListView.setAdapter(dialogListAdapter);

        final AlertDialog dialog = builder.create();

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        WindowManager.LayoutParams wmlp = dialog.getWindow().getAttributes();

        wmlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

        databaseReference.child("shopDetails").child(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shopName.setText(dataSnapshot.child("name").getValue(String.class));
                if(dataSnapshot.child("discount").exists()){
                    discountTxt.setText(Math.round(dataSnapshot.child("discount").getValue(Double.class)) + "% OFF");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        RecyclerView recyclerView = findViewById(R.id.shop_view_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(this,3));

        productDetails = new ArrayList<>();

        final ShopViewAdapter adapter = new ShopViewAdapter(new ArrayList<ProductDetails>(),this);
        recyclerView.setAdapter(adapter);

        Query query = databaseReference.child("productDetails");

        query.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productDetails = new ArrayList<>();
                categories.add("All");
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ProductDetails p = snapshot.getValue(ProductDetails.class);
                    p.setId(snapshot.getKey());
                    productDetails.add(p);
                    String c = p.getCategory();
                    if(!categories.contains(c)){
                        categories.add(c);
                    }
                }
                adapter.setProducts(productDetails);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        String category = categories.get(i);
                        if(category.equals("All")){
                            query.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    productDetails = new ArrayList<>();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        ProductDetails p = snapshot.getValue(ProductDetails.class);
                                        p.setId(snapshot.getKey());
                                        productDetails.add(p);
                                        String c = p.getCategory();
                                        if(!categories.contains(c)){
                                            categories.add(c);
                                        }
                                    }
                                    adapter.setProducts(productDetails  );
                                    adapter.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        else{
                            query.orderByChild("shopId").equalTo(shopId).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    productDetails = new ArrayList<>();
                                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                        ProductDetails p = snapshot.getValue(ProductDetails.class);
                                        p.setId(snapshot.getKey());
                                        if(p.getCategory().equals(category)){
                                            productDetails.add(p);
                                        }
                                        adapter.setProducts(productDetails);
                                        adapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                        dialog.dismiss();
                    }
                });
            }
        });

        FirebaseStorage.getInstance().getReference().child("shops/" + shopId + "/images/profile_pic/profile_pic.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(ShopViewActivity.this).load(uri).into(profilePic);
            }
        });

        databaseReference.child("cartDetails").child(MainActivity.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    cartViewBtn.setVisibility(View.VISIBLE);
                }
                else
                    cartViewBtn.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
