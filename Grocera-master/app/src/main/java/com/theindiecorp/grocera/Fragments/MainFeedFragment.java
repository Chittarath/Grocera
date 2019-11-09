package com.theindiecorp.grocera.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.HomeActivityRecycler;
import com.theindiecorp.grocera.Adapters.OffersListAdapter;
import com.theindiecorp.grocera.Data.ShopDetails;
import com.theindiecorp.grocera.Data.Sorter;
import com.theindiecorp.grocera.Data.Text;
import com.theindiecorp.grocera.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.RecursiveAction;

public class MainFeedFragment extends Fragment {
    ArrayList<ShopDetails> shopDetails;
    ArrayList<String> shopIds;
    public static String pinCode = "";

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    FusedLocationProviderClient fusedLocationProviderClient;
    public static Location currentLocation;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean locationPermissionsGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    Context mContext;



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_feed,null);

        final TextView emptyHomeScreentxt = view.findViewById(R.id.emptyHomeScreenText);
        final TextView numberOfStores = view.findViewById(R.id.garages_near_you);

        LayoutInflater popupInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View popupView = popupInflater.inflate(R.layout.pop_up_sort, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, true);
        final TextView nearest = popupView.findViewById(R.id.nearest);
        final TextView ratings = popupView.findViewById(R.id.ratings);

        final TextView sort = view.findViewById(R.id.sort_text_view);

        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(sort);
            }
        });

        RecyclerView recyclerView = view.findViewById(R.id.home_main_feed_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        RecyclerView offersRecycler = view.findViewById(R.id.offers_list);
        offersRecycler.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));

        shopDetails = new ArrayList<>();
        shopIds = new ArrayList<>();

        final HomeActivityRecycler adapter = new HomeActivityRecycler(new ArrayList<ShopDetails>(),getContext());
        recyclerView.setAdapter(adapter);

        final OffersListAdapter offersListAdapter = new OffersListAdapter(getContext(),new ArrayList<String>());
        offersRecycler.setAdapter(offersListAdapter);

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child("pinCode").exists()){
                    pinCode = dataSnapshot.child("pinCode").getValue(String.class);
                }
                else{
                    pinCode = "null";
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.child("shopDetails").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                shopDetails = new ArrayList<>();
                String category;
                for(DataSnapshot shop : dataSnapshot.getChildren()){
                    ShopDetails s = shop.getValue(ShopDetails.class);
                    s.setId(shop.getKey());
                    if(s.getPinCode().equals(pinCode)){
                        shopDetails.add(s);
                        if(shop.child("discount").exists()){
                            shopIds.add(shop.getKey());

                        }
                    }
                }
                if(pinCode.equals("null") || shopIds.isEmpty()){
                    for(DataSnapshot shop : dataSnapshot.getChildren()){
                        ShopDetails s = shop.getValue(ShopDetails.class);
                        s.setId(shop.getKey());
                        shopDetails.add(s);
                        if(shop.child("discount").exists()){
                            shopIds.add(shop.getKey());
                        }
                    }
                }
                adapter.setShops(shopDetails);
                adapter.notifyDataSetChanged();
                offersListAdapter.setShopIds(shopIds);
                offersListAdapter.notifyDataSetChanged();
                numberOfStores.setText(shopDetails.size() + " Stores Near You");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        nearest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get current location of user
                getLocationPermission();


                if(currentLocation!=null)
                {
                    int i,j;
                    boolean swapped;
                    for(i=0;i<shopDetails.size();i++){
                        Location shopLoc = new Location("provider");
                        if(shopDetails.get(i).getLng()!=null){
                            shopLoc.setLongitude(shopDetails.get(i).getLng());
                            shopLoc.setLatitude(shopDetails.get(i).getLat());
                            Float distance = currentLocation.distanceTo(shopLoc);
                            shopDetails.get(i).setDistance(distance);

                            swapped = false;
                            for(j=0;j<shopDetails.size()-i;j++){
                                Collections.swap(shopDetails,j,j+1);
                                swapped = true;

                //check if Location Service is enabled
                LocationManager manager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
                if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("Enable Location Service");
                    builder.setMessage("You need to enable location service to access this feature");
                    builder.setNegativeButton("Cancel",null);
                    builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    });
                    builder.show();
                }else{
                    final ArrayList<Sorter> distances = new ArrayList<>();
                    for(int i = 0; i<shopDetails.size();i++){
                        final String currentGarageId = shopDetails.get(i).getId();

                        //get device location
                        LocationRequest mLocationRequest = LocationRequest.create();
                        mLocationRequest.setInterval(60000);
                        mLocationRequest.setFastestInterval(5000);
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        LocationCallback mLocationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if(locationResult == null){
                                    return;
                                }
                                for(Location location : locationResult.getLocations()){
                                    if(location!=null){
                                        currentLocation = location;

                                        databaseReference.child("shopDetails").child(currentGarageId).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                double latitude = dataSnapshot.child("lat").getValue(Double.class);
                                                double longitude = dataSnapshot.child("lng").getValue(Double.class);

                                                Location shopLocation = new Location("provider");
                                                shopLocation.setLatitude(latitude);
                                                shopLocation.setLongitude(longitude);
                                                Float distanceTo = shopLocation.distanceTo(currentLocation);

                                                //Insert distance in sorter order
                                                if(distances.size() == 0){
                                                    distances.add(new Sorter(currentGarageId, distanceTo));
                                                }else{
                                                    if (distances.get(distances.size() - 1).getRating() <= distanceTo) {
                                                        distances.add(new Sorter(currentGarageId, distanceTo));
                                                    } else {
                                                        int pos = distances.size();
                                                        while (pos != 0 && (distances.get(pos - 1).getRating() >= distanceTo)) {
                                                            pos--;
                                                        }
                                                        distances.add(pos, new Sorter(currentGarageId, distanceTo));
                                                    }
                                                }

                                                temporaryShops.clear();
                                                //Sort the shops list according to the rating
                                                //Find the mechanic with distances corresponding to the distances ArrayList
                                                for(int j=0;j<distances.size();j++){
                                                    //find the shops
                                                    for(int k=0; k<shopDetails.size();k++){
                                                        if(shopDetails.get(k).getId().equals(distances.get(j).getGarageId())){
                                                            temporaryShops.add(shopDetails.get(k));
                                                        }
                                                    }
                                                }

                                                if(temporaryShops.size() == shopDetails.size()){
                                                    adapter.setShops(temporaryShops);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            if(!swapped){
                                break;
                            }
                        }
                    }
                }
            }
        });

        getLocationPermission();
        getDeviceLocation();

        return view;

    }
    private void getLocationPermission(){
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(getContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationPermissionsGranted = true;
            }
        }
        else{
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if(grantResults.length > 0){
                    for(int i=0; i<grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            locationPermissionsGranted = false;
                            return;
                        }
                    }
                    locationPermissionsGranted = true;
                }
        }
    }

    private void getDeviceLocation(){
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        try{
            final Task location = fusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        currentLocation = (Location) task.getResult();
                        if(currentLocation == null){
                            return;
                        }
                    }
                }
            });
        }catch (SecurityException e){
            Log.e("LocationActivity: ", e.getMessage() );
        }
    }
}
