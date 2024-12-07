package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.fyp1.Adapter.RecycleRequestAdapter;
import com.example.fyp1.Model.RecycleRequest;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class MakeRecycleRequestList extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecycleRequestAdapter adapter;
    private List<RecycleRequest> recycleRequests;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private Spinner spinnerRequestFilter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_recycle_request_list);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("RecycleRequest");

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh data when pulled down
            String selectedFilter = spinnerRequestFilter.getSelectedItem().toString();
            filterRequests(selectedFilter);
        });

        recyclerView = findViewById(R.id.rv_requests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recycleRequests = new ArrayList<>();
        adapter = new RecycleRequestAdapter(recycleRequests, this);
        recyclerView.setAdapter(adapter);

        setupSpinner();
        fetchUserEmailAndRequests();
    }

    private void setupSpinner() {
        spinnerRequestFilter = findViewById(R.id.spinner_request_filter);
        String[] filterOptions = { "All Request", "Pending", "Approved", "Completed", "Rejected" };
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRequestFilter.setAdapter(spinnerAdapter);

        spinnerRequestFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = filterOptions[position];
                filterRequests(selectedFilter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterRequests(String filter) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                switch (filter) {
                    case "All Request":
                        queryRecycleRequests(userEmail);
                        break;
                    case "Pending":
                        queryRecycleRequestsByStatus(userEmail, "Pending");
                        break;
                    case "Approved":
                        queryRecycleRequestsByStatus(userEmail, "Approved");
                        break;
                    case "Completed":
                        queryRecycleRequestsByStatus(userEmail, "Completed");
                        break;
                    case "Rejected":
                        queryRecycleRequestsByStatus(userEmail, "Rejected");
                        break;
                }
            }
        }
    }

    private void queryRecycleRequests(String userEmail) {
        databaseReference.orderByChild("userEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        recycleRequests.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String id = snapshot.getKey();
                            String date = snapshot.child("PickUpDate").getValue() != null ? snapshot.child("PickUpDate").getValue().toString() : "";
                            String itemName = snapshot.child("ItemType").getValue() != null ? snapshot.child("ItemType").getValue().toString() : "";
                            String imageUri = snapshot.child("imageUri").getValue() != null ? snapshot.child("imageUri").getValue().toString() : "";
                            String requestStatus = snapshot.child("requestStatus").getValue() != null ? snapshot.child("requestStatus").getValue().toString() : "";
                            String quantity = snapshot.child("Quantity").getValue() != null ? snapshot.child("Quantity").getValue().toString() : "";
                            String requestAddress = snapshot.child("RequestAddress").getValue() != null ? snapshot.child("RequestAddress").getValue().toString() : "";

                            recycleRequests.add(new RecycleRequest(id, date, itemName, imageUri, requestStatus, quantity, requestAddress));
                        }
                        adapter.notifyDataSetChanged();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MakeRecycleRequestList", "Database error: " + databaseError.getMessage());
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    private void queryRecycleRequestsByStatus(String userEmail, String status) {
        databaseReference.orderByChild("userEmail").equalTo(userEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        recycleRequests.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String requestStatus = snapshot.child("requestStatus").getValue() != null ? snapshot.child("requestStatus").getValue().toString() : "";
                            if (requestStatus.equals(status)) {
                                String id = snapshot.getKey();
                                String date = snapshot.child("PickUpDate").getValue() != null ? snapshot.child("PickUpDate").getValue().toString() : "";
                                String itemName = snapshot.child("ItemType").getValue() != null ? snapshot.child("ItemType").getValue().toString() : "";
                                String imageUri = snapshot.child("imageUri").getValue() != null ? snapshot.child("imageUri").getValue().toString() : "";
                                String quantity = snapshot.child("Quantity").getValue() != null ? snapshot.child("Quantity").getValue().toString() : "";
                                String requestAddress = snapshot.child("RequestAddress").getValue() != null ? snapshot.child("RequestAddress").getValue().toString() : "";

                                recycleRequests.add(new RecycleRequest(id, date, itemName, imageUri, requestStatus, quantity, requestAddress));
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("MakeRecycleRequestList", "Database error: " + databaseError.getMessage());
                        if (swipeRefreshLayout != null) {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                });
    }

    private void fetchUserEmailAndRequests() {
        if (!isNetworkAvailable()) {
            MotionToast.Companion.darkToast(this,
                    "No Internet Connection",
                    "Please check your internet connection.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
            return;
        }
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                queryRecycleRequests(userEmail);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void addRecycleRequest(View view) {
        Intent intent = new Intent(MakeRecycleRequestList.this, LocationPickerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MakeRecycleRequestList.this, NormalUserHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}