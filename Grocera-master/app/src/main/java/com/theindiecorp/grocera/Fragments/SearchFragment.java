package com.theindiecorp.grocera.Fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.HomeActivityRecycler;
import com.theindiecorp.grocera.Adapters.ProductSearchAdapter;
import com.theindiecorp.grocera.Data.ProductDetails;
import com.theindiecorp.grocera.Data.ShopDetails;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    HomeActivityRecycler shopAdapter;
    ProductSearchAdapter productSearchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);

        final RecyclerView shopRecyclerView = view.findViewById(R.id.search_fragment_shop_recycler);
        shopRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        shopAdapter = new HomeActivityRecycler(new ArrayList<ShopDetails>(),getContext());
        shopRecyclerView.setAdapter(shopAdapter);

        final RecyclerView productRecyclerView = view.findViewById(R.id.search_fragment_product_recycler);
        productRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productSearchAdapter = new ProductSearchAdapter(getContext(),new ArrayList<String>());
        productRecyclerView.setAdapter(productSearchAdapter);

        final android.widget.SearchView searchView = view.findViewById(R.id.fragment_search_search_bar);

        searchView.setQueryHint("Search");
        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setIconified(false);
            }
        });

        final TextView showStores = view.findViewById(R.id.show_stores);
        final TextView showProducts = view.findViewById(R.id.show_products);

        showStores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productRecyclerView.setVisibility(View.GONE);
                shopRecyclerView.setVisibility(View.VISIBLE);
                showStores.setTextColor(getResources().getColor(R.color.colorPrimary));
                showProducts.setTextColor(getResources().getColor(android.R.color.black));
            }
        });

        showProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shopRecyclerView.setVisibility(View.GONE);
                productRecyclerView.setVisibility(View.VISIBLE);
                showProducts.setTextColor(getResources().getColor(R.color.colorPrimary));
                showStores.setTextColor(getResources().getColor(android.R.color.black));
            }
        });


        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Query q = FirebaseDatabase.getInstance().getReference("shopDetails")
                        .orderByChild("name")
                        .startAt(query)
                        .endAt(query + "\uf8ff");
                q.addValueEventListener(shopListener);

                Query q1 = FirebaseDatabase.getInstance().getReference("productDetails")
                        .orderByChild("name")
                        .startAt(query)
                        .endAt(query + "\uf8ff");
                q1.addValueEventListener(productListener);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Query q = FirebaseDatabase.getInstance().getReference("shopDetails")
                        .orderByChild("name")
                        .startAt(newText)
                        .endAt(newText + "\uf8ff");
                q.addValueEventListener(shopListener);

                Query q1 = FirebaseDatabase.getInstance().getReference("productDetails")
                        .orderByChild("name")
                        .startAt(newText)
                        .endAt(newText + "\uf8ff");
                q1.addValueEventListener(productListener);
                return false;
            }
        });

        return view;
    }

    ValueEventListener shopListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<ShopDetails> shops = new ArrayList<>();
            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                ShopDetails shop = snapshot.getValue(ShopDetails.class);
                shop.setId(snapshot.getKey());
                shops.add(shop);
            }
            shopAdapter.setShops(shops);
            shopAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };

    ValueEventListener productListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            ArrayList<String> products = new ArrayList<>();
            for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                ProductDetails p = snapshot.getValue(ProductDetails.class);
                products.add(p.getId());
            }
            productSearchAdapter.setProducts(products);
            productSearchAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
}
