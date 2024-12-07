package com.example.fyp1;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class RecycleRequestDetailsActivity extends AppCompatActivity {

    private TextView recyclablesValueTextView, quantityValueTextView, pickupDateValueTextView, collectorValueTextView, locationValueTextView, remarkTitle, remarkValue;
    private ImageView displayImage;
    private Button deleteButton;
    private DatabaseReference databaseReference;
    private String requestStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_detail);

        // Initialize views
        recyclablesValueTextView = findViewById(R.id.recyclablesValueTextView);
        quantityValueTextView = findViewById(R.id.quantityValueTextView);
        pickupDateValueTextView = findViewById(R.id.pickupDateValueTextView);
        collectorValueTextView = findViewById(R.id.collectorValueTextView);
        locationValueTextView = findViewById(R.id.locationValueTextView);
        displayImage = findViewById(R.id.displayImage);
        deleteButton = findViewById(R.id.deleteButton);
        remarkTitle = findViewById(R.id.remarkLabelTextView);
        remarkValue = findViewById(R.id.remarkValueTextView);

        // Initially hide remark fields
        remarkTitle.setVisibility(View.GONE);
        remarkValue.setVisibility(View.GONE);

        // Get data from intent
        Intent intent = getIntent();
        String requestId = intent.getStringExtra("REQUEST_ID");
        String itemType = intent.getStringExtra("ITEM_TYPE");
        String pickUpDate = intent.getStringExtra("PICKUP_DATE");
        String quantity = intent.getStringExtra("QUANTITY");
        String requestAddress = intent.getStringExtra("REQUEST_ADDRESS");
        String imageUrl = intent.getStringExtra("IMAGE_URL");

        // Display data
        recyclablesValueTextView.setText(itemType);
        quantityValueTextView.setText(quantity + "kg");
        pickupDateValueTextView.setText(pickUpDate);
        locationValueTextView.setText(requestAddress);

        if (!isNetworkAvailable()) {
            MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                    "No Internet Connection",
                    "Please check your internet settings.",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            return; // Exit if there is no internet connection
        }

        // Load image using Picasso
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.get().load(imageUrl).into(displayImage);
        }

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("RecycleRequest").child(requestId);

        // Fetch requestStatus and potentially Remark
        databaseReference.child("requestStatus").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                requestStatus = dataSnapshot.getValue(String.class);

                if (requestStatus != null && !requestStatus.equals("Pending")) {
                    // Make remark fields visible
                    remarkTitle.setVisibility(View.VISIBLE);
                    remarkValue.setVisibility(View.VISIBLE);

                    // Fetch Remark and set it to remarkValue
                    databaseReference.child("Remark").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String remark = dataSnapshot.getValue(String.class);
                            if (remark != null) {
                                remarkValue.setText(remark);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                                    "Remark Load Failed",
                                    "Failed to load remark",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                        "Request Status Load Failed",
                        "Failed to load request status",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });

        // Fetch VendorlocationID and set companyName to collectorValueTextView
        databaseReference.child("VendorlocationID").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String vendorLocationID = dataSnapshot.getValue(String.class);

                if (vendorLocationID != null) {
                    DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference("RecycleLocation").child(vendorLocationID);
                    locationReference.child("companyName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot locationSnapshot) {
                            String companyName = locationSnapshot.getValue(String.class);
                            if (companyName != null) {
                                collectorValueTextView.setText(companyName);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError locationError) {
                            MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                                    "Company Name Load Failed",
                                    "Failed to load company name",
                                    MotionToastStyle.ERROR,
                                    MotionToast.GRAVITY_BOTTOM,
                                    MotionToast.LONG_DURATION,
                                    ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Toast.makeText(RecycleRequestDetailsActivity.this, "Failed to load VendorlocationID", Toast.LENGTH_SHORT).show();

            }
        });

        // Set up delete button
        deleteButton.setOnClickListener(v -> {
            if (!isNetworkAvailable()) {
                MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                        "No Internet Connection",
                        "Please check your internet settings.",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                return; // Exit if there is no internet connection
            }

            if (requestStatus != null && requestStatus.equals("Pending")) {
                showDeleteConfirmationDialog(requestId);
            } else {
                MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                        "Request Cannot Be Deleted",
                        "This request cannot be deleted as it is no longer pending.",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }

    private void showDeleteConfirmationDialog(String requestId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete this request?")
                .setPositiveButton("Yes", (dialog, which) -> deleteRequest(requestId))
                .setNegativeButton("No", null)
                .show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void deleteRequest(String requestId) {
        databaseReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                        "Request Deleted",
                        "Request deleted successfully",
                        MotionToastStyle.SUCCESS,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                Intent intent = new Intent(RecycleRequestDetailsActivity.this, MakeRecycleRequestList.class);
                startActivity(intent);
            } else {
                MotionToast.Companion.darkToast(RecycleRequestDetailsActivity.this,
                        "Request Deletion Failed",
                        "Failed to delete request",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(RecycleRequestDetailsActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        });
    }
}
