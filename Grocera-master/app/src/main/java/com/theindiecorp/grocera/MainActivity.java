package com.theindiecorp.grocera;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.theindiecorp.grocera.Data.CartDetails;
import com.theindiecorp.grocera.Data.ProductDetails;
import com.theindiecorp.grocera.Data.ShopDetails;
import com.theindiecorp.grocera.Fragments.CartFragment;
import com.theindiecorp.grocera.Fragments.MainFeedFragment;
import com.theindiecorp.grocera.Fragments.ProfileFragment;
import com.theindiecorp.grocera.Fragments.SearchFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "stupid"; // TODO
    public static String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public static String userEmail;
    String eventIdFromIntent = "";

    private static final String USER_ID = "USER_ID";
    private static final String EDITABLE = "EDITABLE";

    public static boolean alreadyHasCart = false;
    public static CartDetails firstItemOfCart = new CartDetails();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragment = new MainFeedFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_search:
                    fragment = new SearchFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_cart:
                    fragment = new CartFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_profile:
                    fragment = new ProfileFragment();
                    loadFragment(fragment);
                    return true;
            }
            return false;
        }
    };

    private boolean loadFragment(Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Intent intent = getIntent();
        if (intent.hasExtra("intentType")) {
            if (intent.getStringExtra("intentType").equals("startActivityFromNotification")) {
                eventIdFromIntent = intent.getStringExtra("link");
            }
        }

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(task.isSuccessful()){
                    String token = task.getResult().getToken();
                    FirebaseDatabase.getInstance().getReference("messageTokens").child(userId).setValue(token);
                }
            }
        });

        loadFragment(new MainFeedFragment());

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("cartDetails").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    alreadyHasCart = true;
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        CartDetails c = snapshot.getValue(CartDetails.class);
                        firstItemOfCart = c;
                        break;
                    }
                }
                else{
                    alreadyHasCart = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
