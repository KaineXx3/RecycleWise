package com.example.fyp1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fyp1.Adapter.ViewRecycleRequestListAdapter;
import com.example.fyp1.Model.ViewRecycleRequestFromUserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class ViewRecycleRequestFromUser extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ViewRecycleRequestListAdapter adapter;
    private ArrayList<ViewRecycleRequestFromUserModel> recycleRequestList;
    private ArrayList<ViewRecycleRequestFromUserModel> filteredRecycleRequestList;
    private FirebaseAuth mAuth;
    private DatabaseReference registeredUsersRef, recycleLocationRef, recycleRequestRef;
    private Spinner filterSpinner;
    private String[] filterOptions = {"All Request", "Pending", "Approved", "Completed", "Rejected"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_recycle_request_from_user);

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        setupSwipeRefresh();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.Viewrequests);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recycleRequestList = new ArrayList<>();
        filteredRecycleRequestList = new ArrayList<>();
        adapter = new ViewRecycleRequestListAdapter(this, filteredRecycleRequestList);
        recyclerView.setAdapter(adapter);

        setupFilterSpinner();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            if (userEmail != null) {
                fetchRecycleRequests(userEmail);
            }
        }
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(

                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });
    }

    private void setupFilterSpinner() {
        filterSpinner = findViewById(R.id.spinner_filter);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(spinnerAdapter);

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterRequests(filterOptions[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void filterRequests(String filter) {
        filteredRecycleRequestList.clear();
        for (ViewRecycleRequestFromUserModel request : recycleRequestList) {
            if (filter.equals("All Request") || request.getRequestStatus().equals(filter)) {
                filteredRecycleRequestList.add(request);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void refreshData() {
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
                recycleRequestList.clear();
                filteredRecycleRequestList.clear();
                adapter.notifyDataSetChanged();
                fetchRecycleRequests(userEmail);
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void fetchRecycleRequests(String userEmail) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        registeredUsersRef = database.getReference("Registered Users");
        recycleLocationRef = database.getReference("RecycleLocation");
        recycleRequestRef = database.getReference("RecycleRequest");

        registeredUsersRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String email = userSnapshot.child("email").getValue(String.class);
                        if (email != null && email.equals(userEmail)) {
                            checkRecycleLocation(email);
                        }
                    }
                } else {
                    showError("User Not Found", "No user found with this email.");
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching registered user data", error.toException());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void checkRecycleLocation(String userEmail) {
        recycleLocationRef.orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot locationSnapshot : snapshot.getChildren()) {
                        String locationID = locationSnapshot.child("locationID").getValue(String.class);
                        if (locationID != null) {
                            fetchRecycleRequestsByLocation(locationID);
                        }
                    }
                } else {
                    showInfo("Recycle Location Not Set", "You have not set up the recycle location");
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error fetching recycle location data", error.toException());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void fetchRecycleRequestsByLocation(String locationID) {
        recycleRequestRef.orderByChild("VendorlocationID").equalTo(locationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(false);

                if (snapshot.exists()) {
                    recycleRequestList.clear();
                    for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                        ViewRecycleRequestFromUserModel request = requestSnapshot.getValue(ViewRecycleRequestFromUserModel.class);
                        if (request != null) {
                            Log.d("DataFetch", "Fetched ItemType: " + request.getItemType() + ", PickUpDate: " + request.getPickUpDate());
                            recycleRequestList.add(request);
                        }
                    }
                    filterRequests(filterSpinner.getSelectedItem().toString());
                } else {
                    Toast.makeText(ViewRecycleRequestFromUser.this, "No recycle requests found for this location.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("FirebaseError", "Error fetching recycle request data", error.toException());
            }
        });
    }

    private void showError(String title, String message) {
        MotionToast.Companion.darkToast(
                this,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
        );
    }


    private void showInfo(String title, String message) {
        MotionToast.Companion.darkToast(
                this,
                title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.LONG_DURATION,
                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold)
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any references or listeners if needed
    }
}