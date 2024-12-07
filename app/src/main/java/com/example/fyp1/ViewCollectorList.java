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
import com.example.fyp1.Adapter.CollectorListAdapter;
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

public class ViewCollectorList extends AppCompatActivity implements CollectorListAdapter.OnCollectorClickListener {
    private RecyclerView recyclerView;
    private CollectorListAdapter collectorAdapter;
    private List<Collector> collectors;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DatabaseReference collectorsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_collector);

        // Initialize Firebase reference
        collectorsRef = FirebaseDatabase.getInstance().getReference("RecycleLocation");

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);


        swipeRefreshLayout.setOnRefreshListener(this::fetchCollectorsFromFirebase);

        recyclerView = findViewById(R.id.recyclerViewCollectors);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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
        collectorAdapter = new CollectorListAdapter(this, collectors, this);
        recyclerView.setAdapter(collectorAdapter);

        // Initial data fetch
        fetchCollectorsFromFirebase();
    }

    private void fetchCollectorsFromFirebase() {
        // Show refresh indicator
        if (!isNetworkAvailable()) {
            MotionToast.Companion.darkToast(this,
                    "No Internet Connection",
                    "Please check your internet connection.",
                    MotionToastStyle.ERROR,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helveticabold));
        }

        swipeRefreshLayout.setRefreshing(true);

        collectorsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                collectors.clear();
                for (DataSnapshot collectorSnapshot : snapshot.getChildren()) {
                    String companyName = collectorSnapshot.child("companyName").getValue(String.class);
                    String imageUrl = collectorSnapshot.child("imageUrl").getValue(String.class);
                    String locationID = collectorSnapshot.child("locationID").getValue(String.class);
                    String province = collectorSnapshot.child("province").getValue(String.class);
                    String state = collectorSnapshot.child("state").getValue(String.class);

                    if (companyName != null && imageUrl != null && locationID != null &&
                            province != null && state != null) {
                        Collector collector = new Collector(companyName, imageUrl, locationID, province, state);
                        collectors.add(collector);
                    }
                }
                collectorAdapter.notifyDataSetChanged();

                // Hide refresh indicator
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Toast.makeText(ViewCollectorList.this,
                        "Failed to load collectors: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Hide refresh indicator
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    public void onCollectorClick(Collector collector) {
        Intent intent = new Intent(this, CollectorDetailActivity.class);
        intent.putExtra("locationID", collector.getLocationID());
        intent.putExtra("collector_Name", collector.getCompanyName());
        startActivity(intent);
    }
}