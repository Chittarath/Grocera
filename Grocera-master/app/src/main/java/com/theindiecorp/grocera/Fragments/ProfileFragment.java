package com.theindiecorp.grocera.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.applozic.mobicommons.commons.core.utils.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.theindiecorp.grocera.Adapters.RecentOrdersAdapter;
import com.theindiecorp.grocera.ChatActivity;
import com.theindiecorp.grocera.Data.OrderDetails;
import com.theindiecorp.grocera.FAQActivity;
import com.theindiecorp.grocera.LoginActivity;
import com.theindiecorp.grocera.ManageAddressActivity;
import com.theindiecorp.grocera.ManageProfileActivity;
import com.theindiecorp.grocera.R;
import com.theindiecorp.grocera.RecentOrdersActivity;

import java.util.ArrayList;
import java.util.List;

import io.kommunicate.KmChatBuilder;
import io.kommunicate.Kommunicate;
import io.kommunicate.callbacks.KmCallback;


public class ProfileFragment extends Fragment {

    TextView userName,userEmail,userPhone,viewMoreOrders;
    Button editProfileBtn,logout, manageAddressBtn , helpbtn , userinfo;
    ArrayList<OrderDetails> orderDetails = new ArrayList<>();
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

    public static final String APP_ID = "148d45b3834202449a60f478858ab6a89";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile,container,false);

        Kommunicate.init(getContext(), APP_ID);

        userName = view.findViewById(R.id.profile_view_name);
        userEmail = view.findViewById(R.id.profile_view_mail_id);
        userPhone = view.findViewById(R.id.profile_view_phone_number);
        editProfileBtn = view.findViewById(R.id.profile_view_edit_btn);
        logout = view.findViewById(R.id.profile_view_logout_btn);
        manageAddressBtn = view.findViewById(R.id.profile_view_manage_address_btn);
        helpbtn=view.findViewById(R.id.profile_view_help_btn);
        userinfo=view.findViewById(R.id.user_profile);
        Button myordersBtn = view.findViewById(R.id.my_orders);

        myordersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), RecentOrdersActivity.class));
            }
        });

        databaseReference.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userName.setText(dataSnapshot.child("name").getValue(String.class));
                userEmail.setText(dataSnapshot.child("email").getValue(String.class));
                userPhone.setText(dataSnapshot.child("phone").getValue(String.class) + " |");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ManageProfileActivity.class));

            }
        });

        logout.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);

            }
        });

        manageAddressBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ManageAddressActivity.class));
            }
        });

        helpbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<String> botList = new ArrayList(); botList.add("bot1"); //enter your integrated bot Ids
                new KmChatBuilder(getContext()).setChatName("Support")
                        .setBotIds(botList)
                        .launchChat(new KmCallback() {
                            @Override
                            public void onSuccess(Object message) {
                                Utils.printLog(getContext(), "ChatTest", "Success : " + message);
                            }

                            @Override
                            public void onFailure(Object error) {
                                Utils.printLog(getContext(), "ChatTest", "Failure : " + error);
                            }
                        });
            }
        });

        return view;
    }
}

