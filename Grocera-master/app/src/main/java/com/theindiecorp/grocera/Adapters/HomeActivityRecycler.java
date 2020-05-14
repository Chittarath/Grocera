package com.theindiecorp.grocera.Adapters;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.ShopDetails;
import com.theindiecorp.grocera.Fragments.MainFeedFragment;
import com.theindiecorp.grocera.MainActivity;
import com.theindiecorp.grocera.R;
import com.theindiecorp.grocera.ShopViewActivity;

import java.util.ArrayList;

public class HomeActivityRecycler extends RecyclerView.Adapter<HomeActivityRecycler.MyViewHolder> {
    private ArrayList<ShopDetails> dataSet;
    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private Context context;

    public int setShops(ArrayList<ShopDetails> dataSet){
        this.dataSet = dataSet;
        return dataSet.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView shopName, shopAddress, distance, rating;
        private ImageView profilePic;

        public MyViewHolder(View itemView){
            super(itemView);
            this.shopName = itemView.findViewById(R.id.home_item_shop_name);
            this.shopAddress = itemView.findViewById(R.id.home_item_address);
            this.distance = itemView.findViewById(R.id.ETA);
            this.rating = itemView.findViewById(R.id.rating);
            this.profilePic = itemView.findViewById(R.id.profile_pic);
        }
    }

    public HomeActivityRecycler(ArrayList<ShopDetails> data, Context context){
        this.dataSet = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_activity_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int listPosition) {
        final ShopDetails shopDetails = dataSet.get(listPosition);
        holder.shopAddress.setText(shopDetails.getAddress());
        holder.shopName.setText(shopDetails.getName());

        FirebaseStorage.getInstance().getReference().child("shops/" + shopDetails.getId() + "/images/profile_pic/profile_pic.jpeg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.profilePic);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(shopDetails.getStatus().equals("close")){
                    Toast.makeText(context, holder.shopName.getText()+ " is closed", Toast.LENGTH_SHORT).show();
                }

                else{
                    if(MainActivity.firstItem.getShopId().equals(shopDetails.getId())){
                        context.startActivity(new Intent(context, ShopViewActivity.class).putExtra("shopId", shopDetails.getId()));
                    }
                    else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setCancelable(true);
                        builder.setMessage("Your cart has items from another store. Clear your cart?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                databaseReference.child("cartDetails").child(MainActivity.userId).removeValue();
                                context.startActivity(new Intent(context, ShopViewActivity.class).putExtra("shopId",shopDetails.getId()));
                            }
                        });
                        builder.setNegativeButton("No", null);
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
            }
        });

        float eta = 0;
        if(shopDetails.getLat() != null && shopDetails.getLng() != null){
            Location mechLocation = new Location("");
            mechLocation.setLatitude(shopDetails.getLat());
            mechLocation.setLongitude(shopDetails.getLng());
            if(MainFeedFragment.currentLocation != null){
                eta = MainFeedFragment.currentLocation.distanceTo(mechLocation);
                eta = eta / 40;
            }
            if(eta < 75)
                holder.distance.setText(eta + " minutes away");
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

}
