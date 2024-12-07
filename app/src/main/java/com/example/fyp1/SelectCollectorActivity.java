package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fyp1.Adapter.CollectorAdapter;
import com.example.fyp1.Model.Collector;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class SelectCollectorActivity extends AppCompatActivity implements CollectorAdapter.OnCollectorClickListener {
    private RecyclerView recyclerView;
    private CollectorAdapter collectorAdapter;
    private List<Collector> collectors;
    private TextView stateDisplay;

    private String locationAddress;
    private int quantity;
    private String pickupDate;
    private String recyclableItem;
    private String imageUri;
    private String userEmail;
    private String selectedState;
    private SwipeRefreshLayout swipeRefreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector_information);

        recyclerView = findViewById(R.id.recyclerViewCollectors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        stateDisplay = findViewById(R.id.stateDisplay);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),   // Keep original left padding
                    topInset,                // Set top padding to adjust for status bar or notch
                    view.getPaddingRight(),  // Keep original right padding
                    view.getPaddingBottom()  // Keep original bottom padding
            );

            return insets;
        });



        collectors = new ArrayList<>();
        collectorAdapter = new CollectorAdapter(this, collectors, this);
        recyclerView.setAdapter(collectorAdapter);



        // Retrieve data from Intent
        Intent intent = getIntent();
        if (intent != null) {
            locationAddress = intent.getStringExtra("location_address");
            quantity = intent.getIntExtra("quantity", -1);
            pickupDate = intent.getStringExtra("pickup_date");
            recyclableItem = intent.getStringExtra("recyclable_item");
            imageUri = intent.getStringExtra("image_uri");
            userEmail= intent.getStringExtra("user_email");

            // Handle the image URI
            handleImageUri(imageUri);

            // Update state display and get the selected state
            selectedState = updateStateDisplay(locationAddress);

            // Fetch data from Firebase
            fetchCollectorsFromFirebase();
        }

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchCollectorsFromFirebase(); // Fetch data when swiped
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Fetch data from Firebase for the first time
        fetchCollectorsFromFirebase();

    }

    private void handleImageUri(String imageUri) {
        if (imageUri != null) {
            switch (imageUri) {
                case "no_image":
                    //Toast.makeText(this, "No image was selected", Toast.LENGTH_SHORT).show();
                    break;
                case "camera_image":
                    //Toast.makeText(this, "Image was taken with camera", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    //Toast.makeText(this, "Image URI: " + imageUri + "email: " + userEmail, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            //Toast.makeText(this, "Image URI is null", Toast.LENGTH_SHORT).show();
        }
    }

    private String updateStateDisplay(String locationAddress) {
        String[] malaysianStates = {
                "Johor", "Kedah", "Kelantan", "Malacca", "Negeri Sembilan",
                "Pahang", "Penang", "Perak", "Perlis", "Sabah", "Sarawak",
                "Selangor", "Terengganu", "Kuala Lumpur", "Labuan", "Putrajaya"
        };

        for (String state : malaysianStates) {
            if (locationAddress != null && locationAddress.contains(state)) {
                stateDisplay.setText(state);
                return state;
            }
        }

        // Optional: Handle the case where no state was matched
        stateDisplay.setText("Unknown State");
        return "Unknown State";
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void fetchCollectorsFromFirebase() {
        if (!isNetworkAvailable()) {
            MotionToast.Companion.darkToast(this,
                    "No Internet Connection",
                    "Please check your internet connection.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
            return; // Exit if there is no internet connection
        }
        DatabaseReference collectorsRef = FirebaseDatabase.getInstance().getReference("RecycleLocation");
        collectorsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectors.clear();
                for (DataSnapshot collectorSnapshot : snapshot.getChildren()) {
                    String companyName = collectorSnapshot.child("companyName").getValue(String.class);
                    String imageUrl = collectorSnapshot.child("imageUrl").getValue(String.class);
                    String locationID = collectorSnapshot.child("locationID").getValue(String.class);
                    String province = collectorSnapshot.child("province").getValue(String.class);
                    String state = collectorSnapshot.child("state").getValue(String.class);

                    if (companyName != null && imageUrl != null && locationID != null && province != null && state != null) {
                        if (state.equalsIgnoreCase(selectedState)) {
                            Collector collector = new Collector(companyName, imageUrl, locationID, province, state);
                            collectors.add(collector);
                        }
                    }
                }
                collectorAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(SelectCollectorActivity.this, "Failed to load collectors: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCollectorClick(Collector collector) {
        Intent intent = new Intent(this, RequestConfirmationActivity.class); // Replace MainActivity with your actual next activity
        intent.putExtra("locationID", collector.getLocationID());
        intent.putExtra("collector_Name",collector.getCompanyName());
        intent.putExtra("location_address", locationAddress);
        intent.putExtra("quantity", quantity);
        intent.putExtra("pickup_date", pickupDate);
        intent.putExtra("recyclable_item", recyclableItem);
        intent.putExtra("image_uri", imageUri);
        intent.putExtra("user_Email", userEmail);
        startActivity(intent);
    }
}